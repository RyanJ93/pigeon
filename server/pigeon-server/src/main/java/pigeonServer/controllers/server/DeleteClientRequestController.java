package pigeonServer.controllers.server;

import pigeonServer.exceptions.ControllerServerException;
import pigeonServer.models.server.clientRequest.DeleteClientRequest;
import pigeonServer.services.MessageService;
import pigeonServer.support.Logger;

import java.util.ArrayList;
import java.util.HashMap;

public class DeleteClientRequestController extends ClientRequestController {
    private void logAction(MessageService messageService){
        ArrayList<String>  lastProcessedMessageIDs = messageService.getLastProcessedMessageIDs();
        DeleteClientRequest deleteClientRequest = (DeleteClientRequest)this.clientRequest;
        String logMessage;
        if ( lastProcessedMessageIDs.size() == 0 ){
            logMessage = "No message deleted ";
            logMessage += " from the " + ( deleteClientRequest.getSent() ? "sent" : "read" ) + " stack of the user ";
            logMessage += deleteClientRequest.getAuthenticatedUser().getUsername() + ".";
        }else{
            logMessage = "Deleted messages with IDs ";
            logMessage += String.join(", ", lastProcessedMessageIDs);
            logMessage += " from " + ( deleteClientRequest.getSent() ? "sent" : "read" ) + " stack of the user ";
            logMessage += deleteClientRequest.getAuthenticatedUser().getUsername() + ".";
        }
        Logger.log(logMessage);
    }

    public DeleteClientRequestController(DeleteClientRequest deleteClientRequest){
        super(deleteClientRequest);
    }

    public HashMap<String, Object> handle() throws ControllerServerException {
        try{
            DeleteClientRequest deleteClientRequest = (DeleteClientRequest)this.clientRequest;
            MessageService messageService = new MessageService();
            messageService.setUser(deleteClientRequest.getAuthenticatedUser());
            messageService.delete(deleteClientRequest.getMessages(), deleteClientRequest.getSent());
            this.logAction(messageService);
            return null;
        }catch(Exception ex){
            throw new ControllerServerException("An error occurred while processing the request.", ex);
        }
    }
}
