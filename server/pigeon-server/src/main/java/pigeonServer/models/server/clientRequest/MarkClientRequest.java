package pigeonServer.models.server.clientRequest;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import pigeonServer.controllers.server.MarkClientRequestController;
import pigeonServer.exceptions.InvalidRequestServerException;
import java.util.ArrayList;

public class MarkClientRequest extends ClientRequest {
    private ArrayList<String> messages = null;
    private boolean read = true;

    public MarkClientRequest(){
        this.action = "mark";
    }

    public MarkClientRequest processPayload(JsonObject payload) throws InvalidRequestServerException {
        if ( payload == null ){
            throw new InvalidRequestServerException("Invalid request.");
        }
        JsonArray messageList = payload.has("messages") ? payload.get("messages").getAsJsonArray() : null;
        ArrayList<String> messages = new ArrayList<>();
        if ( messageList != null ){
            for ( JsonElement entry : messageList ){
                String messageID = entry.getAsString();
                if ( messageID != null && !messageID.isEmpty() ){
                    messages.add(messageID);
                }
            }
        }
        if ( messages.size() == 0 ){
            throw new InvalidRequestServerException("No valid message ID provided.");
        }
        this.messages = messages;
        this.read = payload.has("read") && payload.get("read").getAsBoolean();
        return this;
    }

    public MarkClientRequestController getControllerInstance(){
        return new MarkClientRequestController(this);
    }

    public ArrayList<String> getMessages(){
        return this.messages;
    }

    public boolean getRead(){
        return this.read;
    }
}
