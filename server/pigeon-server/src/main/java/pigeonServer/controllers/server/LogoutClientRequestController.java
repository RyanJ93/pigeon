package pigeonServer.controllers.server;

import pigeonServer.exceptions.ControllerServerException;
import pigeonServer.models.server.AuthToken;
import pigeonServer.models.server.clientRequest.LogoutClientRequest;
import pigeonServer.support.Logger;
import java.util.HashMap;

public class LogoutClientRequestController extends ClientRequestController {
    private void logAction(){
        Logger.log("User logged out successfully.");
    }

    public LogoutClientRequestController(LogoutClientRequest logoutClientRequest){
        super(logoutClientRequest);
    }

    public HashMap<String, Object> handle() throws ControllerServerException {
        try{
            LogoutClientRequest logoutClientRequest = (LogoutClientRequest)this.clientRequest;
            AuthToken authToken = AuthToken.find(logoutClientRequest.getAuthenticationToken());
            if ( authToken != null ){
                authToken.delete();
                this.logAction();
            }
            return null;
        }catch(Exception ex){
            throw new ControllerServerException("An error occurred while processing the request.", ex);
        }
    }
}
