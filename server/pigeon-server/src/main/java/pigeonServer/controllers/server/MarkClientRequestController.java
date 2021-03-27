package pigeonServer.controllers.server;

import pigeonServer.exceptions.ControllerServerException;
import pigeonServer.models.server.clientRequest.MarkClientRequest;
import pigeonServer.services.MessageService;
import pigeonServer.support.Logger;
import java.util.ArrayList;
import java.util.HashMap;

public class MarkClientRequestController extends ClientRequestController {
    private void logAction(MessageService messageService){
        ArrayList<String> lastProcessedMessageIDs = messageService.getLastProcessedMessageIDs();
        MarkClientRequest markClientRequest = (MarkClientRequest)this.clientRequest;
        String logMessage;
        if ( lastProcessedMessageIDs.size() == 0 ){
            logMessage = "No message marked as " + ( markClientRequest.getRead() ? "read" : "unread" ) + " by user ";
            logMessage += "\"" + markClientRequest.getAuthenticatedUser().getUsername() + "\".";
        }else{
            logMessage = "Marked messages with IDs ";
            logMessage += String.join(", ", messageService.getLastProcessedMessageIDs()) + " marked as ";
            logMessage += " as " + ( markClientRequest.getRead() ? "read" : "unread" ) + " by user ";
            logMessage += "\"" + markClientRequest.getAuthenticatedUser().getUsername() + "\".";
        }
        Logger.log(logMessage);
    }

    public MarkClientRequestController(MarkClientRequest markClientRequest){
        super(markClientRequest);
    }

    public HashMap<String, Object> handle() throws ControllerServerException {
        try{
            MarkClientRequest markClientRequest = (MarkClientRequest)this.clientRequest;
            MessageService messageService = new MessageService();
            messageService.setUser(markClientRequest.getAuthenticatedUser());
            messageService.mark(markClientRequest.getMessages(), markClientRequest.getRead());
            this.logAction(messageService);
            return null;
        }catch(Exception ex){
            throw new ControllerServerException("An error occurred while processing the request.", ex);
        }
    }
}
