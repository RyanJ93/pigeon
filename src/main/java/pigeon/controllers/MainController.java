package pigeon.controllers;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import pigeon.Main;
import pigeon.exceptions.Exception;
import pigeon.exceptions.UnauthorizedException;
import pigeon.exceptions.UserNotFoundException;
import pigeon.models.Message;
import pigeon.models.User;
import pigeon.services.UserService;
import pigeon.support.Connector;
import pigeon.support.MessageList;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainController extends Controller implements Initializable {
    protected static FXMLLoader loader;
    private static Stage stage;

    public static void show(){
        try{
            if ( MainController.stage == null ){
                URL url = Objects.requireNonNull(MainController.class.getClassLoader().getResource("views/main.fxml"));
                MainController.loader = new FXMLLoader(url);
                Parent root = MainController.loader.load();
                MainController.stage = new Stage();
                MainController.stage.setTitle("Pigeon");
                MainController.stage.setScene(new Scene(root, 1000, 600));
                MainController.stage.setOnCloseRequest(event -> {
                    Platform.exit();
                    System.exit(0);
                });
                MainController.stage.setResizable(false);
            }
            MainController.stage.toFront();
            MainController.stage.show();
            if ( MainController.loader != null ){
                ((MainController)MainController.loader.getController()).reset();
            }
        }catch(IOException ex){
            ex.printStackTrace();
        }
    }

    public static void hide(){
        if ( MainController.stage != null && MainController.stage.isShowing() ){
            MainController.stage.hide();
        }
    }

    public static MainController getControllerInstance(){
        return MainController.loader == null ? null : MainController.loader.getController();
    }

    public static void setOffline(boolean isOffline){
        Platform.runLater(() -> MainController.stage.setTitle(isOffline ? "Pigeon [offline]" : "Pigeon"));
    }

    @FXML
    private ListView<Message> messageList;

    @FXML
    private MenuBar menuBar;

    @FXML
    private Label messageSubject;

    @FXML
    private Label messageSender;

    @FXML
    private Label messageDate;

    @FXML
    private Label messageRecipient;

    @FXML
    private WebView messageBody;

    @FXML
    private MenuItem markMenuItem;

    @FXML
    private MenuItem markAllMenuItem;

    @FXML
    private MenuItem replyMenuItem;

    @FXML
    private MenuItem replyAllMenuItem;

    @FXML
    private MenuItem forwardMenuItem;

    @FXML
    private MenuItem changeListMenuItem;

    @FXML
    private MenuItem loggedUserMenuItem;

    @FXML
    private AnchorPane messageViewer;

    private Message selectedMessage = null;
    private boolean isSentList = false;

    @FXML
    private void handleNewMessageAction(){
        EditorController.show();
    }

    @FXML
    private void handleCloseAction(){
        Platform.exit();
        System.exit(0);
    }

    @FXML
    private void handleLogoutAction(){
        this.logout();
    }

    @FXML
    private void handleDeleteAction(){
        this.deleteSelectedMessage();
    }

    @FXML
    private void handleMarkAction(){
        this.markSelectedMessage();
    }

    @FXML
    private void handleMarkAllMessagesAsReadAction(){
        this.markAllMessagesAsRead();
    }

    @FXML
    private void handleForwardAction(){
        this.forward();
    }

    @FXML
    private void handleChangeListAction(){
        this.isSentList = !this.isSentList;
        this.reloadList();
    }

    @FXML
    private void handleRefreshAction(){
        this.reloadList();
    }

    @FXML
    private void handleAboutAction(){
        AboutController.show();
    }

    @FXML
    private void handleReplyAction(){
        this.reply(false);
    }

    @FXML
    private void handleReplyAllAction(){
        this.reply(true);
    }

    private void logout(){
        try{
            MainController.hide();
            UserService userService = new UserService();
            userService.logout();
        }catch(Exception ex){
            ex.printStackTrace();
            ex.getAlert().show();
        }catch(ConnectException ex){
            ex.printStackTrace();
            MainController.makeConnectionIssueAlert("Unable to logout").show();
        }catch(IOException ex){
            ex.printStackTrace();
            MainController.makeAlert("Unable to logout", "An error occurred while logging you out, please retry later").show();
        }finally{
            LoginController.show();
        }
    }

    private void deleteSelectedMessage(){
        if ( this.selectedMessage != null && MainController.checkOnlineStatus() ){
            String message = "Do you really wish to delete this message? \nNote that this action is permanent and cannot be undone.";
            Optional<ButtonType> result = MainController.makeConfirmation("Message delete confirmation", message).showAndWait();
            if ( result.isPresent() && result.get() == ButtonType.OK ){
                new Thread(() -> {
                    try{
                        synchronized(this){
                            String modeName = this.isSentList ? MessageList.MODE_SENT : MessageList.MODE_RECEIVED;
                            Connector connector = new Connector();
                            connector.delete(this.selectedMessage.getID(), this.isSentList);
                            MessageList.getList(modeName).removeMessage(this.selectedMessage);
                            this.selectedMessage = null;
                        }
                    }catch(UnauthorizedException | UserNotFoundException ex){
                        Main.requestLogin();
                    }catch(Exception ex){
                        ex.printStackTrace();
                        Platform.runLater(() -> ex.getAlert().show());
                    }catch(ConnectException ex){
                        ex.printStackTrace();
                        Platform.runLater(() -> MainController.makeConnectionIssueAlert("Unable to delete the message").show());
                    }catch(IOException ex){
                        ex.printStackTrace();
                        Platform.runLater(() -> MainController.makeAlert("Unable to delete the message", "An error occurred while deleting the message, please retry later").show());
                    }
                }).start();
            }
        }
    }

    private void resetMessageViewer(){
        this.messageBody.getEngine().loadContent("");
        this.messageRecipient.setText("");
        this.messageSubject.setText("");
        this.messageSender.setText("");
        this.messageDate.setText("");
    }

    private void displayMessage(Message message){
        this.messageViewer.setVisible(message != null);
        this.selectedMessage = message;
        this.resetMessageViewer();
        if ( message != null ){
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss");
            ArrayList<String> recipientList = new ArrayList<>();
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            message.getRecipients().forEach(user -> recipientList.add(user.getUsername() + "@" + Connector.getHostname()));
            String recipient = recipientList.isEmpty() ? "-" : String.join(", ", recipientList);
            this.markMenuItem.setText(message.getRead() && !message.getSent() ? "Mark as unread" : "Mark as read");
            this.messageSender.setText(message.getSender().getUsername() + "@" + Connector.getHostname());
            this.messageDate.setText(dateFormat.format(message.getDate()));
            this.messageBody.getEngine().loadContent(message.getBody());
            this.messageSubject.setText(message.getSubject());
            this.messageRecipient.setText("To: " + recipient);
            if ( !message.getRead() && !message.getSent() ){
                this.markSelectedMessage();
            }
        }
    }

    private void refreshMenuItemsDisableStatus(){
        if ( this.isSentList ){
            this.replyAllMenuItem.setDisable(true);
            this.markAllMenuItem.setDisable(true);
            this.forwardMenuItem.setDisable(false);
            this.replyMenuItem.setDisable(true);
            this.markMenuItem.setDisable(true);
        }else{
            this.replyAllMenuItem.setDisable(false);
            this.markAllMenuItem.setDisable(false);
            this.forwardMenuItem.setDisable(false);
            this.replyMenuItem.setDisable(false);
            this.markMenuItem.setDisable(false);
        }
    }

    private void reloadList(){
        try{
            this.refreshMenuItemsDisableStatus();
            this.messageViewer.setVisible(false);
            this.resetMessageViewer();
            if ( this.isSentList ){
                MessageList messageList = MessageList.getList(MessageList.MODE_SENT);
                this.messageList.setItems(messageList.getMessageObservableList());
                this.changeListMenuItem.setText("Show received messages");
                messageList.stopListener().reload();
            }else{
                MessageList messageList = MessageList.getList(MessageList.MODE_RECEIVED);
                this.messageList.setItems(messageList.getMessageObservableList());
                this.changeListMenuItem.setText("Show sent messages");
                messageList.reload().setupListener();
            }
            this.displayMessage(this.messageList.getItems().isEmpty() ? null : this.messageList.getItems().get(0));
            if ( this.messageList.getItems().isEmpty() ){
                this.messageList.setPlaceholder(new Label("No message found"));
            }
        }catch(UnauthorizedException | UserNotFoundException ex){
            Main.requestLogin();
        }catch(Exception ex){
            ex.printStackTrace();
            ex.getAlert().show();
        }catch(ConnectException ex){
            ex.printStackTrace();
            EditorController.makeConnectionIssueAlert("Unable to fetch messages").show();
        }catch(IOException ex){
            ex.printStackTrace();
            MainController.makeAlert("Unable to fetch messages", "An error occurred while fetching messages, please retry later").show();
        }
    }

    private void markSelectedMessage(){
        if ( this.selectedMessage != null && !this.selectedMessage.getSent() && MainController.checkOnlineStatus() ){
            boolean read = this.markMenuItem.getText().equals("Mark as read");
            String[] ids = new String[]{this.selectedMessage.getID()};
            new Thread(() -> {
                try{
                    Connector connector = new Connector();
                    connector.mark(read, ids);
                    synchronized(this){
                        this.selectedMessage.setRead(read);
                    }
                    this.redrawList();
                }catch(UnauthorizedException | UserNotFoundException ex){
                    Main.requestLogin();
                }catch(Exception ex){
                    ex.printStackTrace();
                    Platform.runLater(() -> ex.getAlert().show());
                }catch(ConnectException ex){
                    ex.printStackTrace();
                    Platform.runLater(() -> MainController.makeConnectionIssueAlert("Unable to mark the message").show());
                }catch(IOException ex){
                    ex.printStackTrace();
                    Platform.runLater(() -> MainController.makeAlert("Unable to mark messages", "An error occurred while marking the message, please retry later").show());
                }
            }).start();
        }
    }

    private void redrawList(){
        Platform.runLater(() -> {
            this.messageList.refresh();
            if ( this.selectedMessage != null && !this.selectedMessage.getSent() ){
                this.markMenuItem.setText(this.selectedMessage.getRead() ? "Mark as unread" : "Mark as read");
            }
        });
    }

    private void markAllMessagesAsRead(){
        ObservableList<Message> messages = this.messageList.getItems();
        if ( !this.isSentList && messages.size() > 0 && MainController.checkOnlineStatus() ){
            String[] ids = new String[messages.size()];
            for ( int i = 0 ; i < ids.length ; i++ ){
                ids[i] = messages.get(i).getID();
            }
            new Thread(() -> {
                try{
                    Connector connector = new Connector();
                    connector.mark(true, ids);
                    MessageList.getList(MessageList.MODE_RECEIVED).setReadForAll(true);
                    this.redrawList();
                }catch(UnauthorizedException | UserNotFoundException ex){
                    Main.requestLogin();
                }catch(Exception ex){
                    ex.printStackTrace();
                    Platform.runLater(() -> ex.getAlert().show());
                }catch(ConnectException ex){
                    ex.printStackTrace();
                    Platform.runLater(() -> MainController.makeConnectionIssueAlert("Unable to mark messages").show());
                }catch(IOException ex){
                    ex.printStackTrace();
                    Platform.runLater(() -> MainController.makeAlert("Unable to fetch messages", "An error occurred while fetching messages, please retry later").show());
                }
            }).start();
        }
    }

    private void reply(boolean all){
        if ( this.selectedMessage != null && !this.selectedMessage.getSent() ){
            EditorController.show();
            EditorController editorController = EditorController.getControllerInstance();
            if ( editorController != null ){
                int messageHandlingMode = all ? EditorController.REPLY_ALL_MODE : EditorController.REPLY_MODE;
                editorController.setContextMessage(this.selectedMessage).setMessageHandlingMode(messageHandlingMode);
            }
        }
    }

    private void forward(){
        if ( this.selectedMessage != null ){
            EditorController.show();
            EditorController editorController = EditorController.getControllerInstance();
            if ( editorController != null ){
                editorController.setContextMessage(this.selectedMessage).setMessageHandlingMode(EditorController.FORWARD_MODE);
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb){
        final String os = System.getProperty("os.name");
        if ( os != null && os.toLowerCase(Locale.ROOT).startsWith("mac") ){
            this.menuBar.useSystemMenuBarProperty().set(true);
        }
    }

    public MainController reset(){
        this.messageList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> this.displayMessage(observable.getValue()));
        try{
            this.messageList.setCellFactory(messageCardController -> new MessageCardController());
            UserService userService = new UserService();
            User loggedInUser = userService.getActiveUser();
            this.loggedUserMenuItem.setText("Logged in as " + loggedInUser.getUsername() + "@" + Connector.getHostname());
        }catch(Exception | IOException ex){
            ex.printStackTrace();
        }
        this.reloadList();
        return this;
    }
}
