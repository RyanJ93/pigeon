package pigeonServer.models.server;

import pigeonServer.services.StorageService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class SentMessage extends Message {
    public static SentMessage find(User targetUser, String id) throws IOException {
        SentMessage sentMessage = null;
        StorageService storageService = new StorageService();
        HashMap<String, String> document = storageService.setNamespace("sent_messages").setEntity(targetUser.getID()).findOne(id);
        if ( document != null ){
            sentMessage = new SentMessage();
            sentMessage.setProperties(document);
        }
        return sentMessage;
    }

    public static ArrayList<SentMessage> getAll(User targetUser, boolean unreadOnly, Date start) throws IOException {
        ArrayList<SentMessage> messages = new ArrayList<>();
        StorageService storageService = new StorageService();
        storageService.setNamespace("sent_messages").setEntity(targetUser.getID());
        ArrayList<HashMap<String, String>> documents = storageService.find(null);
        for ( HashMap<String, String> document : documents ){
            if ( !unreadOnly || document.get("read").equals("0") ){
                SentMessage sentMessage = new SentMessage();
                sentMessage.setProperties(document).setTargetUser(targetUser);
                if ( start == null || start.compareTo(sentMessage.getDate()) < 0 ){
                    messages.add(sentMessage);
                }
            }
        }
        return messages;
    }

    public Message save() throws IOException {
        HashMap<String, String> properties = this.getProperties();
        StorageService storageService = new StorageService();
        storageService.setNamespace("sent_messages").setEntity(this.targetUser.getID());
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
        storageService.setNamespace("sent_messages").setEntity(this.targetUser.getID()).deleteOne(this.id);
        this.bound = false;
        return this;
    }
}
