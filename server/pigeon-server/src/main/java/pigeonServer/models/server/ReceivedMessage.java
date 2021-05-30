package pigeonServer.models.server;

import pigeonServer.services.StorageService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class ReceivedMessage extends Message {
    public static ReceivedMessage find(User targetUser, String id) throws IOException {
        ReceivedMessage receivedMessage = null;
        StorageService storageService = new StorageService();
        HashMap<String, String> document = storageService.setNamespace("received_messages").setEntity(targetUser.getID()).findOne(id);
        if ( document != null ){
            receivedMessage = new ReceivedMessage();
            receivedMessage.setProperties(document);
        }
        return receivedMessage;
    }

    public static ArrayList<ReceivedMessage> getAll(User targetUser, boolean unreadOnly, Date start) throws IOException {
        ArrayList<ReceivedMessage> messages = new ArrayList<>();
        StorageService storageService = new StorageService();
        storageService.setNamespace("received_messages").setEntity(targetUser.getID());
        ArrayList<HashMap<String, String>> documents = storageService.find(null, "date", StorageService.ORDER_BY_DESC);
        for ( HashMap<String, String> document : documents ){
            if ( !unreadOnly || document.get("read").equals("0") ){
                ReceivedMessage receivedMessage = new ReceivedMessage();
                receivedMessage.setProperties(document).setTargetUser(targetUser);
                if ( start == null || start.compareTo(receivedMessage.getDate()) < 0 ){
                    messages.add(receivedMessage);
                }
            }
        }
        return messages;
    }

    public Message save() throws IOException {
        HashMap<String, String> properties = this.getProperties();
        StorageService storageService = new StorageService();
        storageService.setNamespace("received_messages").setEntity(this.targetUser.getID());
        if ( this.bound ){
            storageService.update(this.id, properties, true);
        }else{
            storageService.insert(this.id, properties);
            this.bound = true;
        }
        return this;
    }

    public Message delete() throws IOException {
        StorageService storageService = new StorageService();
        storageService.setNamespace("received_messages").setEntity(this.targetUser.getID()).deleteOne(this.id);
        this.bound = false;
        return this;
    }
}
