package pigeon.support;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import pigeon.exceptions.Exception;
import pigeon.models.Message;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;

public class MessageList {
    public static final String MODE_SENT = "sent";
    public static final String MODE_RECEIVED = "received";

    private static MessageList sentList = null;
    private static MessageList receivedList = null;

    public static MessageList getList(String mode){
        MessageList list;
        if ( mode.equals(MessageList.MODE_SENT) ){
            if ( MessageList.sentList == null ){
                MessageList.sentList = new MessageList(MessageList.MODE_SENT);
            }
            list = MessageList.sentList;
        }else{
            if ( MessageList.receivedList == null ){
                MessageList.receivedList = new MessageList(MessageList.MODE_RECEIVED);
            }
            list = MessageList.receivedList;
        }
        return list;
    }

    private final ObservableList<Message> messageObservableList;
    private MessageListener messageListener = null;
    private final HashSet<String> messageIndex;
    private String mode = "received";

    private MessageList(String mode){
        this.messageObservableList = FXCollections.observableArrayList();
        this.messageIndex = new HashSet<>();
        this.setMode(mode);
    }

    public MessageList setMode(String mode){
        this.mode = mode;
        return this;
    }

    public String getMode(){
        return this.mode;
    }

    public ObservableList<Message> getMessageObservableList(){
        return this.messageObservableList;
    }

    public synchronized boolean addMessage(Message message){
        AtomicBoolean inserted = new AtomicBoolean(false);
        inserted.set(!this.messageIndex.contains(message.getID()));
        Platform.runLater(() -> {
            if ( inserted.get() ){
                this.messageObservableList.add(0, message);
            }
        });
        return inserted.get();
    }

    public void removeMessage(Message message){
        Platform.runLater(() -> {
            this.messageIndex.remove(message.getID());
            for ( int i = 0 ; i < this.messageObservableList.size() ; i++ ){
                if ( this.messageObservableList.get(i).getID().equals(message.getID()) ){
                    this.messageObservableList.remove(i);
                    break;
                }
            }
        });
    }

    public void setReadForAll(boolean read){
        Platform.runLater(() -> {
            for ( Message message : this.messageObservableList ){
                message.setRead(read);
            }
        });
    }

    public MessageList reload() throws IOException, Exception {
        boolean isSentList = this.mode.equals(MessageList.MODE_SENT);
        this.messageObservableList.clear();
        this.messageIndex.clear();
        ArrayList<Message> messages = isSentList ? Message.getSent(null, false) : Message.getReceived(null, false);
        if ( messages != null ){
            for ( Message message : messages ){
                if ( !this.messageIndex.contains(message.getID()) ){
                    this.messageObservableList.add(message);
                    this.messageIndex.add(message.getID());
                }
            }
        }
        return this;
    }

    public MessageList setupListener(){
        if ( this.messageListener == null && this.mode.equals(MessageList.MODE_RECEIVED) ){
            this.messageListener = new MessageListener(this);
            this.messageListener.start();
        }
        return this;
    }

    public MessageList stopListener(){
        if ( this.messageListener != null ){
            this.messageListener.interrupt();
            this.messageListener = null;
        }
        return this;
    }
}
