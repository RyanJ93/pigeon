package pigeonServer.models.server.clientRequest;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import pigeonServer.controllers.server.DeleteClientRequestController;
import pigeonServer.exceptions.InvalidRequestServerException;
import java.util.ArrayList;

public class DeleteClientRequest extends ClientRequest {
    private ArrayList<String> messages = null;
    private boolean sent = false;

    public DeleteClientRequest(){
        this.action = "delete";
    }

    public DeleteClientRequest processPayload(JsonObject payload) throws InvalidRequestServerException {
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
        this.sent = payload.has("sent") && payload.get("sent").getAsBoolean();
        return this;
    }

    public DeleteClientRequestController getControllerInstance(){
        return new DeleteClientRequestController(this);
    }

    public ArrayList<String> getMessages(){
        return this.messages;
    }

    public boolean getSent(){
        return this.sent;
    }
}
