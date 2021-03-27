package pigeonServer.models.server.clientRequest;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import pigeonServer.controllers.server.SendClientRequestController;
import pigeonServer.exceptions.InvalidRequestServerException;
import java.util.ArrayList;

public class SendClientRequest extends ClientRequest {
    private ArrayList<String> recipients = null;
    private String subject = null;
    private String body = null;

    public SendClientRequest(){
        this.action = "send";
    }

    public SendClientRequest processPayload(JsonObject payload) throws InvalidRequestServerException {
        if ( payload == null ){
            throw new InvalidRequestServerException("Invalid request.");
        }
        JsonArray to = payload.has("to") ? payload.get("to").getAsJsonArray() : null;
        String subject = payload.has("subject") ? payload.get("subject").getAsString() : "";
        String body = payload.has("body") ? payload.get("body").getAsString() : "";
        if ( body.isEmpty() ){
            throw new InvalidRequestServerException("No message body given.");
        }
        this.recipients = new ArrayList<>();
        if ( to != null ){
            for ( JsonElement element : to ){
                String recipient = element.getAsString();
                if ( recipient != null && !recipient.isEmpty() ){
                    this.recipients.add(recipient);
                }
            }
        }
        if ( this.recipients.size() == 0 ){
            throw new InvalidRequestServerException("No recipient defined.");
        }
        this.subject = subject;
        this.body = body;
        return this;
    }

    public SendClientRequestController getControllerInstance(){
        return new SendClientRequestController(this);
    }

    public ArrayList<String> getRecipients(){
        return this.recipients;
    }

    public String getSubject(){
        return this.subject;
    }

    public String getBody(){
        return this.body;
    }
}
