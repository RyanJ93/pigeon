package pigeonServer.services;

import pigeonServer.models.server.Message;
import pigeonServer.models.server.ReceivedMessage;
import pigeonServer.models.server.SentMessage;
import pigeonServer.models.server.User;
import java.io.IOException;
import java.util.ArrayList;

public class MessageService {
    private User user;
    private ArrayList<String> lastProcessedMessageIDs = new ArrayList<>();

    public MessageService setUser(User user){
        this.user = user;
        return this;
    }

    public User getUser(){
        return this.user;
    }

    public ArrayList<String> getLastProcessedMessageIDs(){
        return this.lastProcessedMessageIDs;
    }

    public MessageService send(ArrayList<String> recipientsUsername, String subject, String body) throws IOException {
        this.lastProcessedMessageIDs.clear();
        ArrayList<User> recipients = new ArrayList<>();
        for ( String recipientUsername : recipientsUsername ){
            if ( !recipientUsername.isEmpty() ){
                User recipient = User.findByUsername(recipientUsername);
                if ( recipient != null ){
                    recipients.add(recipient);
                }
            }
        }
        for ( User recipient : recipients ){
            ReceivedMessage receivedMessage = new ReceivedMessage();
            receivedMessage.setTargetUser(recipient).setRecipients(recipients).setSender(this.user);
            receivedMessage.setSubject(subject).setBody(body).save();
            this.lastProcessedMessageIDs.add(receivedMessage.getID());
        }
        SentMessage sentMessage = new SentMessage();
        sentMessage.setTargetUser(this.user).setRecipients(recipients).setSender(this.user);
        sentMessage.setSubject(subject).setBody(body).save();
        this.lastProcessedMessageIDs.add(sentMessage.getID());
        return this;
    }

    public MessageService deleteOne(String messageID, boolean sent) throws IOException {
        this.lastProcessedMessageIDs.clear();
        Message message = sent ? SentMessage.find(this.user, messageID) : ReceivedMessage.find(this.user, messageID);
        if ( message != null ){
            message.delete();
            this.lastProcessedMessageIDs.add(message.getID());
        }
        return this;
    }

    public MessageService delete(ArrayList<String> messageIDs, boolean sent) throws IOException {
        for ( String messageID : messageIDs ){
            this.deleteOne(messageID, sent);
        }
        return this;
    }

    public MessageService mark(ArrayList<String> messageIDs, boolean read) throws IOException {
        this.lastProcessedMessageIDs.clear();
        for ( String messageID : messageIDs ){
            ReceivedMessage receivedMessage = ReceivedMessage.find(this.user, messageID);
            if ( receivedMessage != null ){
                receivedMessage.setRead(read).save();
                this.lastProcessedMessageIDs.add(receivedMessage.getID());
            }
        }
        return this;
    }
}
