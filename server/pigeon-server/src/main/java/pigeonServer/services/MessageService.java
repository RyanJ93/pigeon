package pigeonServer.services;

import pigeonServer.models.server.Message;
import pigeonServer.models.server.ReceivedMessage;
import pigeonServer.models.server.SentMessage;
import pigeonServer.models.server.User;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MessageService {
    private static final String UNKNOWN_RECIPIENTS_MESSAGE_PATH = "messages/unknown_recipients.html";
    private static final String UNKNOWN_RECIPIENTS_MESSAGE_SUBJECT = "Unable to deliver your message to some recipients.";
    private static final String WELCOME_MESSAGE_PATH = "messages/welcome.html";
    private static final String WELCOME_MESSAGE_SUBJECT = "Welcome to Pigeon!";

    private User user;
    private final ArrayList<String> lastProcessedMessageIDs = new ArrayList<>();

    private String getMessageContent(String message, HashMap<String, String> parameters) throws IOException {
        BufferedReader bufferedReader = null;
        try{
            InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(message);
            bufferedReader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(inputStream), StandardCharsets.UTF_8));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ( ( line = bufferedReader.readLine() ) != null ){
                stringBuilder.append(line);
            }
            String content = stringBuilder.toString();
            if ( parameters != null ){
                for ( Map.Entry<String, String> parameter : parameters.entrySet() ){
                    content = content.replace("{" + parameter.getKey() + "}", parameter.getValue());
                }
            }
            return content;
        }finally{
            if ( bufferedReader != null ){
                bufferedReader.close();
            }
        }
    }

    private String buildUnknownRecipientsMessageContent(ArrayList<String> unknownRecipients) throws IOException {
        HashMap<String, String> parameters = new HashMap<>();
        StringBuilder listBuilder = new StringBuilder();
        for ( String unknownRecipient : unknownRecipients ){
            listBuilder.append("<li>").append(unknownRecipient).append("</li>");
        }
        parameters.put("list", listBuilder.toString());
        parameters.put("username", this.user.getUsername());
        return this.getMessageContent(MessageService.UNKNOWN_RECIPIENTS_MESSAGE_PATH, parameters);
    }

    private String buildWelcomeMessageContent() throws IOException {
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("username", this.user.getUsername());
        return this.getMessageContent(MessageService.WELCOME_MESSAGE_PATH, parameters);
    }

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

    public SentMessage send(ArrayList<String> recipientsUsername, String subject, String body) throws IOException {
        this.lastProcessedMessageIDs.clear();
        ArrayList<String> unknownRecipients = new ArrayList<>();
        ArrayList<User> recipients = new ArrayList<>();
        for ( String recipientUsername : recipientsUsername ){
            if ( !recipientUsername.isEmpty() ){
                User recipient = User.findByUsername(recipientUsername);
                if ( recipient == null ){
                    unknownRecipients.add(recipientUsername);
                }else{
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
        if ( unknownRecipients.size() > 0 ){
            ReceivedMessage receivedMessage = new ReceivedMessage();
            receivedMessage.setTargetUser(this.user).setRecipients(new ArrayList<>(Collections.singletonList(this.user)));
            receivedMessage.setBody(this.buildUnknownRecipientsMessageContent(unknownRecipients));
            receivedMessage.setSubject(MessageService.UNKNOWN_RECIPIENTS_MESSAGE_SUBJECT);
            receivedMessage.setSender(User.getSystemUser()).save();
        }
        return sentMessage;
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

    public MessageService sendWelcomeMessage() throws IOException {
        ReceivedMessage receivedMessage = new ReceivedMessage();
        receivedMessage.setTargetUser(this.user).setRecipients(new ArrayList<>(Collections.singletonList(this.user)));
        receivedMessage.setBody(this.buildWelcomeMessageContent()).setSubject(MessageService.WELCOME_MESSAGE_SUBJECT);
        receivedMessage.setSender(User.getSystemUser()).save();
        return this;
    }
}
