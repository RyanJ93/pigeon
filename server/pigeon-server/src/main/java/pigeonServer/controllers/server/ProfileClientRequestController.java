package pigeonServer.controllers.server;

import pigeonServer.exceptions.ControllerServerException;
import pigeonServer.models.server.clientRequest.ProfileClientRequest;
import pigeonServer.support.Logger;
import pigeonServer.models.server.User;
import java.util.HashMap;

public class ProfileClientRequestController extends ClientRequestController {
    private void logAction(User user){
        String logMessage = "Returned information about user " + user.getUsername();
        Logger.log(logMessage);
    }

    public ProfileClientRequestController(ProfileClientRequest profileClientRequest){
        super(profileClientRequest);
    }

    public HashMap<String, Object> handle() throws ControllerServerException {
        try{
            ProfileClientRequest profileClientRequest = (ProfileClientRequest)this.clientRequest;
            User user = profileClientRequest.getAuthenticatedUser();
            HashMap<String, Object> response = new HashMap<>();
            response.put("id", user.getID());
            response.put("username", user.getUsername());
            this.logAction(user);
            return response;
        }catch(Exception ex){
            throw new ControllerServerException("An error occurred while processing the request.", ex);
        }
    }
}
