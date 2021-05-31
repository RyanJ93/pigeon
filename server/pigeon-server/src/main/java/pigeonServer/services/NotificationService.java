package pigeonServer.services;

import pigeonServer.models.server.ReceivedMessage;
import pigeonServer.models.server.User;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class NotificationService extends Service {
    private static final String UNKNOWN_RECIPIENTS_MESSAGE_PATH = "notifications/unknown_recipients.html";
    private static final String UNKNOWN_RECIPIENTS_MESSAGE_SUBJECT = "Unable to deliver your message to some recipients.";
    private static final String WELCOME_MESSAGE_PATH = "notifications/welcome.html";
    private static final String WELCOME_MESSAGE_SUBJECT = "Welcome to Pigeon!";
    private static final String PASSWORD_CHANGED_MESSAGE_PATH = "notifications/password_changed.html";
    private static final String PASSWORD_CHANGED_MESSAGE_SUBJECT = "Your password has been changed!";

    private User user;

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

    public NotificationService setUser(User user){
        this.user = user;
        return this;
    }

    public User getUser(){
        return this.user;
    }

    public NotificationService sendUnknownRecipientsNotification(ArrayList<String> unknownRecipients) throws IOException {
        HashMap<String, String> parameters = new HashMap<>();
        StringBuilder listBuilder = new StringBuilder();
        for ( String unknownRecipient : unknownRecipients ){
            listBuilder.append("<li>").append(unknownRecipient).append("</li>");
        }
        parameters.put("list", listBuilder.toString());
        parameters.put("username", this.user.getUsername());
        String content = this.getMessageContent(NotificationService.UNKNOWN_RECIPIENTS_MESSAGE_PATH, parameters);
        ReceivedMessage receivedMessage = new ReceivedMessage();
        receivedMessage.setTargetUser(this.user).setRecipients(new ArrayList<>(Collections.singletonList(this.user)));
        receivedMessage.setSubject(NotificationService.UNKNOWN_RECIPIENTS_MESSAGE_SUBJECT);
        receivedMessage.setSender(User.getSystemUser()).setBody(content).save();
        return this;
    }

    public NotificationService sendWelcomeNotification() throws IOException {
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("username", this.user.getUsername());
        String content = this.getMessageContent(NotificationService.WELCOME_MESSAGE_PATH, parameters);
        ReceivedMessage receivedMessage = new ReceivedMessage();
        receivedMessage.setTargetUser(this.user).setRecipients(new ArrayList<>(Collections.singletonList(this.user)));
        receivedMessage.setBody(content).setSubject(NotificationService.WELCOME_MESSAGE_SUBJECT);
        receivedMessage.setSender(User.getSystemUser()).save();
        return this;
    }

    public NotificationService sendPasswordChangedNotification() throws IOException {
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("username", this.user.getUsername());
        String content = this.getMessageContent(NotificationService.PASSWORD_CHANGED_MESSAGE_PATH, parameters);
        ReceivedMessage receivedMessage = new ReceivedMessage();
        receivedMessage.setTargetUser(this.user).setRecipients(new ArrayList<>(Collections.singletonList(this.user)));
        receivedMessage.setBody(content).setSubject(NotificationService.PASSWORD_CHANGED_MESSAGE_SUBJECT);
        receivedMessage.setSender(User.getSystemUser()).save();
        return this;
    }
}
