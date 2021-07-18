package pigeon.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.web.HTMLEditor;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditorController extends Controller implements Initializable {
    public static final int REPLY_MODE = 1;
    public static final int REPLY_ALL_MODE = 2;
    public static final int FORWARD_MODE = 3;

    protected static FXMLLoader loader;
    private static Stage stage;

    public static void show() {
        try{
            if ( EditorController.stage == null ){
                URL url = Objects.requireNonNull(EditorController.class.getClassLoader().getResource("views/editor.fxml"));
                EditorController.loader = new FXMLLoader(url);
                Parent root = EditorController.loader.load();
                EditorController.stage = new Stage();
                EditorController.stage.setTitle("Compose new message");
                EditorController.stage.setScene(new Scene(root, 800, 600));
                EditorController.stage.setResizable(false);
            }
            EditorController.stage.toFront();
            EditorController.stage.show();
        }catch(IOException ex){
            ex.printStackTrace();
        }
    }

    public static void hide(){
        if ( EditorController.stage != null && EditorController.stage.isShowing() ){
            EditorController.stage.hide();
        }
    }

    public static EditorController getControllerInstance(){
        return EditorController.loader == null ? null : EditorController.loader.getController();
    }

    @FXML
    private TextField recipients;

    @FXML
    private TextField subject;

    @FXML
    private HTMLEditor body;

    @FXML
    private Button sendButton;

    private Message contextMessage;
    private int messageHandlingMode;

    @FXML
    private void handleSendAction(){
        this.sendMessage();
    }

    @FXML
    private void handleDiscardAction(){
        EditorController.hide();
        this.reset();
    }

    private void reset(){
        this.recipients.setText("");
        this.body.setHtmlText("");
        this.subject.setText("");
    }

    private ArrayList<String> getRecipients(boolean usernameOnly){
        String recipientListText = this.recipients.getText();
        ArrayList<String> recipients = new ArrayList<>();
        if ( recipientListText != null && !recipientListText.isEmpty() ){
            if ( usernameOnly ){
                String[] candidateList = recipientListText.split(",");
                ArrayList<String> recipientList = new ArrayList<>();
                for ( String candidate : candidateList ){
                    String[] components = candidate.trim().split("@");
                    if ( components.length == 2 && !recipientList.contains(components[0]) ){
                        recipientList.add(components[0]);
                    }
                }
                recipients = recipientList;
            }else{
                recipients = new ArrayList<>(Arrays.asList(recipientListText.split(",")));
            }
        }
        return recipients;
    }

    private boolean validate(){
        Pattern usernamePattern = Pattern.compile("^[a-zA-Z0-9][a-zA-Z0-9._-]{0,31}$");
        Pattern hostnamePattern = Pattern.compile("^[a-zA-Z0-9._-]+\\.[a-zA-Z0-9]+$");
        ArrayList<String> invalidRecipientList = new ArrayList<>();
        ArrayList<String> recipients = this.getRecipients(false);
        String messages = "";
        for ( String recipient : recipients ){
            recipient = recipient.trim();
            String[] components = recipient.split("@");
            if ( components.length != 2 ){
                if ( !invalidRecipientList.contains(recipient) ){
                    invalidRecipientList.add(recipient);
                }
                continue;
            }
            Matcher usernameMatcher = usernamePattern.matcher(components[0]);
            Matcher hostnameMatcher = hostnamePattern.matcher(components[1]);
            boolean isValid = usernameMatcher.matches() && hostnameMatcher.matches() && !components[0].equalsIgnoreCase("system");
            if ( !isValid && !invalidRecipientList.contains(recipient) ){
                invalidRecipientList.add(recipient);
            }
        }
        if ( recipients.size() == 0 ){
            messages += "You must provide at least one recipient.\n";
        }else if ( invalidRecipientList.size() > 0 ){
            messages += "The following addresses are not valid:\n";
            for ( String invalidRecipient : invalidRecipientList ){
                messages += invalidRecipient + "\n";
            }
        }
        if ( !messages.isEmpty() ){
            EditorController.makeAlert("Invalid message", messages.trim()).show();
        }
        return messages.isEmpty();
    }

    private void sendMessage(){
        if ( this.validate() && EditorController.checkOnlineStatus() ){
            this.sendButton.setDisable(true);
            ArrayList<String> recipients = this.getRecipients(true);
            String subject = this.subject.getText();
            String body = this.body.getHtmlText();
            new Thread(() -> {
                try{
                    Connector connector = new Connector();
                    Message message = connector.send(recipients, subject, body);
                    MessageList.getList(MessageList.MODE_SENT).addMessage(message);
                    Platform.runLater(() -> EditorController.makeAlert("Message sent", "Your message has been sent to recipients.").showAndWait().ifPresent(rs -> {
                        EditorController.hide();
                        this.reset();
                    }));
                }catch(UnauthorizedException | UserNotFoundException ex){
                    Main.requestLogin();
                }catch(Exception ex){
                    ex.printStackTrace();
                    Platform.runLater(() -> ex.getAlert().show());
                }catch(ConnectException ex){
                    ex.printStackTrace();
                    Platform.runLater(() -> EditorController.makeConnectionIssueAlert("Unable to send the message").show());
                }catch(IOException ex){
                    ex.printStackTrace();
                    Platform.runLater(() -> EditorController.makeAlert("Unable to send the message", "An error occurred while sending your message, please retry later").show());
                }finally{
                    Platform.runLater(() -> this.sendButton.setDisable(false));
                }
            }).start();
        }
    }

    private void handleMessageReply(boolean all) throws IOException, Exception {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss");
        String recipients = this.contextMessage.getSender().getUsername() + "@" + Connector.getHostname();
        UserService userService = new UserService();
        User loggedInUser = userService.getActiveUser();
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        if ( all ){
            ArrayList<String> recipientList = new ArrayList<>();
            this.contextMessage.getRecipients().forEach(user -> {
                if ( !user.getID().equals(loggedInUser.getID()) ){
                    recipientList.add(user.getUsername() + "@" + Connector.getHostname());
                }
            });
            if ( recipientList.size() > 0 ){
                recipients += ", " + String.join(", ", recipientList);
            }
        }
        String HTMLBody = "<br /><br /><div style=\"border-left:3px solid #9b59b6;padding-left:6px;color:9b59b6;\">";
        HTMLBody += "<p style=\"margin:0;\">On " + dateFormat.format(this.contextMessage.getDate()) + " ";
        HTMLBody += this.contextMessage.getSender().getUsername() + "@" + Connector.getHostname() + " wrote: </p>";
        HTMLBody += "<br />" + this.contextMessage.getBody() + "<br /></div>";
        String subject = this.contextMessage.getSubject();
        if ( subject.indexOf("Re: ") != 0 ){
            subject = "Re: " + subject;
        }
        this.recipients.setText(recipients);
        this.body.setHtmlText(HTMLBody);
        this.subject.setText(subject);
    }

    private void handleMessageForward(){
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss");
        ArrayList<String> recipientList = new ArrayList<>();
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        this.contextMessage.getRecipients().forEach(user -> recipientList.add(user.getUsername() + "@" + Connector.getHostname()));
        String HTMLBody = "<br /><br /><div style=\"border-left:3px solid #9b59b6;padding-left:6px;color:9b59b6;\">";
        HTMLBody += "<p style=\"margin:0;\">Begin forwarded message: </p><br />";
        HTMLBody += "<p style=\"margin:0;\"><b>From:</b> " + this.contextMessage.getSender().getUsername() + "</p>";
        HTMLBody += "<p style=\"margin:0;\"><b>Subject:</b> " + this.contextMessage.getSubject() + "</p>";
        HTMLBody += "<p style=\"margin:0;\"><b>Date:</b> " + dateFormat.format(this.contextMessage.getDate()) + "</p>";
        HTMLBody += "<p style=\"margin:0;\"><b>To:</b> " + String.join(", ", recipientList) + "</p>";
        HTMLBody += "<br />" + this.contextMessage.getBody() + "<br /></div>";
        String subject = this.contextMessage.getSubject();
        if ( subject.indexOf("Fwd: ") != 0 ){
            subject = "Fwd: " + subject;
        }
        this.body.setHtmlText(HTMLBody);
        this.subject.setText(subject);
        this.recipients.setText("");
    }

    private void handleContextMessage(){
        if ( this.contextMessage == null ){
            this.reset();
        }else{
            try{
                switch ( this.messageHandlingMode ){
                    case EditorController.REPLY_MODE -> this.handleMessageReply(false);
                    case EditorController.REPLY_ALL_MODE -> this.handleMessageReply(true);
                    case EditorController.FORWARD_MODE -> this.handleMessageForward();
                }
                this.body.requestFocus();
            }catch(Exception | IOException ex){
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources){}

    public EditorController setContextMessage(Message contextMessage){
        this.contextMessage = contextMessage;
        this.handleContextMessage();
        return this;
    }

    public Message getContextMessage(){
        return this.contextMessage;
    }

    public EditorController setMessageHandlingMode(int messageHandlingMode){
        this.messageHandlingMode = messageHandlingMode;
        this.handleContextMessage();
        return this;
    }

    public int getMessageHandlingMode(){
        return this.messageHandlingMode;
    }
}
