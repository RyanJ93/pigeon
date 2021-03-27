package pigeonServer.controllers.server;

import pigeonServer.exceptions.ControllerServerException;
import pigeonServer.models.server.*;
import pigeonServer.models.server.clientRequest.FetchClientRequest;
import pigeonServer.support.Logger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

public class FetchClientRequestController extends ClientRequestController {
    private void logAction(){
        FetchClientRequest fetchClientRequest = (FetchClientRequest)this.clientRequest;
        String logMessage = "Loaded message list from the " + ( fetchClientRequest.getSent() ? "sent" : "read" ) + " stack";
        if ( fetchClientRequest.getUnreadOnly() ){
            logMessage += " (unread only)";
        }
        logMessage += " of the user \"" + fetchClientRequest.getAuthenticatedUser().getUsername() + "\"";
        Date start = fetchClientRequest.getStart();
        if ( start != null ){
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            logMessage += " starting from date " + dateFormat.format(start);
        }
        logMessage += ".";
        Logger.log(logMessage);
    }

    public FetchClientRequestController(FetchClientRequest fetchClientRequest){
        super(fetchClientRequest);
    }

    public HashMap<String, Object> handle() throws ControllerServerException {
        try{
            FetchClientRequest fetchClientRequest = (FetchClientRequest)this.clientRequest;
            User user = fetchClientRequest.getAuthenticatedUser();
            HashMap<String, Object> response = new HashMap<>();
            if ( fetchClientRequest.getSent() ){
                ArrayList<SentMessage> messages = SentMessage.getAll(user, fetchClientRequest.getUnreadOnly(), fetchClientRequest.getStart());
                response.put("messages", messages);
            }else{
                ArrayList<ReceivedMessage> messages = ReceivedMessage.getAll(user, fetchClientRequest.getUnreadOnly(), fetchClientRequest.getStart());
                response.put("messages", messages);
            }
            this.logAction();
            return response;
        }catch(Exception ex){
            throw new ControllerServerException("An error occurred while processing the request.", ex);
        }
    }
}
