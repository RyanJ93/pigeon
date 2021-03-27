package pigeonServer.controllers.server;

import pigeonServer.exceptions.ControllerServerException;
import pigeonServer.models.server.clientRequest.SendClientRequest;
import pigeonServer.services.MessageService;
import pigeonServer.support.Logger;
import java.util.HashMap;

public class SendClientRequestController extends ClientRequestController {
    private void logAction(){
        SendClientRequest sendClientRequest = (SendClientRequest)this.clientRequest;
        String logMessage = "Sent message by user \"" + sendClientRequest.getAuthenticatedUser().getUsername() + "\"";
        logMessage += " to the following users: " + String.join(", ", sendClientRequest.getRecipients()) + ".";
        Logger.log(logMessage);
    }

    public SendClientRequestController(SendClientRequest sendClientRequest){
        super(sendClientRequest);
    }

    public HashMap<String, Object> handle() throws ControllerServerException {
        try{
            SendClientRequest sendClientRequest = (SendClientRequest)this.clientRequest;
            MessageService messageService = new MessageService();
            messageService.setUser(sendClientRequest.getAuthenticatedUser());
            messageService.send(sendClientRequest.getRecipients(), sendClientRequest.getSubject(), sendClientRequest.getBody());
            this.logAction();
            return null;
        }catch(Exception ex){
            throw new ControllerServerException("An error occurred while processing the request.", ex);
        }
    }
}
