package pigeon.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.Pane;
import pigeon.models.Message;
import pigeon.support.Connector;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.TimeZone;

public class MessageCardController extends ListCell<Message> implements Initializable {
    private Message message = null;
    private FXMLLoader loader = null;

    @FXML
    private Pane pane;

    @FXML
    private Label sender;

    @FXML
    private Label date;

    @FXML
    private Label subject;

    @FXML
    private Label preview;

    private void setCardProperties(){
        if ( this.message != null ){
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            String sender = this.message.getSender().getUsername() + "@" + Connector.getHostname();
            if ( !this.message.getSent() && !this.message.getRead() ){
                sender = "• " + sender;
            }
            this.sender.setText(sender);
            this.date.setText(this.message.getDate() == null ? "" : dateFormat.format(this.message.getDate()));
            this.subject.setText(this.message.getSubject());
            this.preview.setText(this.message.getPreview());
        }
    }

    @Override
    protected void updateItem(Message message, boolean empty){
        super.updateItem(message, empty);
        if ( message == null || empty ){
            this.setText(null);
            this.setGraphic(null);
        }else{
            if ( this.loader == null ){
                try{
                    URL url = Objects.requireNonNull(this.getClass().getClassLoader().getResource("views/message_card.fxml"));
                    this.loader = new FXMLLoader(url);
                    this.loader.setController(this);
                    this.loader.load();
                }catch(Exception ex){
                    ex.printStackTrace();
                }
            }
            this.setText(null);
            this.setGraphic(this.pane);
            this.message = message;
            this.setCardProperties();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb){}
}
