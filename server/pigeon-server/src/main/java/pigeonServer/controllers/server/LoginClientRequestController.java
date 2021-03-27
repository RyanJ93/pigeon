package pigeonServer.controllers.server;

import pigeonServer.exceptions.ControllerServerException;
import pigeonServer.models.server.AuthToken;
import pigeonServer.models.server.clientRequest.LoginClientRequest;
import pigeonServer.services.UserService;
import pigeonServer.support.Logger;
import java.util.HashMap;

public class LoginClientRequestController extends ClientRequestController {
    private void logAction(){
        LoginClientRequest loginClientRequest = (LoginClientRequest)this.clientRequest;
        Logger.log("User \"" + loginClientRequest.getUsername() + "\" authenticated successfully.");
    }

    public LoginClientRequestController(LoginClientRequest loginClientRequest){
        super(loginClientRequest);
    }

    public HashMap<String, Object> handle() throws ControllerServerException {
        try{
            LoginClientRequest loginClientRequest = (LoginClientRequest)this.clientRequest;
            UserService userService = new UserService();
            AuthToken authToken = userService.authenticateByCredentials(loginClientRequest.getUsername(), loginClientRequest.getPassword());
            HashMap<String, Object> response = new HashMap<>();
            response.put("token", authToken.getToken());
            this.logAction();
            return response;
        }catch(Exception ex){
            throw new ControllerServerException("An error occurred while processing the request.", ex);
        }
    }
}
