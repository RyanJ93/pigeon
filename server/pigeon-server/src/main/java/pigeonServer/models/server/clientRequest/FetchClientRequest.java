package pigeonServer.models.server.clientRequest;

import com.google.gson.JsonObject;
import pigeonServer.controllers.server.FetchClientRequestController;
import pigeonServer.exceptions.InvalidRequestServerException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FetchClientRequest extends ClientRequest {
    private Date start = null;
    private boolean unreadOnly = false;
    private boolean sent = false;

    public FetchClientRequest(){
        this.action = "fetch";
    }

    public FetchClientRequest processPayload(JsonObject payload) throws InvalidRequestServerException {
        if ( payload == null ){
            throw new InvalidRequestServerException("Invalid request.");
        }
        try{
            String startDate = payload.has("start") ? payload.get("start").getAsString() : null;
            this.start = null;
            if ( startDate != null && !startDate.isEmpty() ){
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                this.start = dateFormat.parse(startDate);
            }
            this.unreadOnly = payload.has("unreadOnly") && payload.get("unreadOnly").getAsBoolean();
            this.sent = payload.has("sent") && payload.get("sent").getAsBoolean();
            return this;
        }catch(ParseException ex){
            throw new InvalidRequestServerException("Invalid date format.");
        }
    }

    public FetchClientRequestController getControllerInstance(){
        return new FetchClientRequestController(this);
    }

    public Date getStart(){
        return this.start;
    }

    public boolean getUnreadOnly(){
        return this.unreadOnly;
    }

    public boolean getSent(){
        return this.sent;
    }
}
