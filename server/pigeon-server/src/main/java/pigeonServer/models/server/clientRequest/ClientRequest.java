package pigeonServer.models.server.clientRequest;

import com.google.gson.JsonObject;
import pigeonServer.controllers.server.ClientRequestController;
import pigeonServer.exceptions.InvalidRequestServerException;
import pigeonServer.exceptions.UnauthorizedServerException;
import pigeonServer.exceptions.UserNotFoundServerException;
import pigeonServer.models.server.User;
import pigeonServer.services.UserService;

import java.io.IOException;

public abstract class ClientRequest {
    protected String action;
    protected User authenticatedUser = null;
    protected String authenticationToken = null;

    public ClientRequest checkAuth(JsonObject request) throws UserNotFoundServerException, UnauthorizedServerException, IOException {
        String token = request.has("token") ? request.get("token").getAsString() : "";
        if ( token.isEmpty() ){
            throw new UnauthorizedServerException("No authorization token provided.");
        }
        UserService userService = new UserService();
        this.authenticatedUser = userService.authenticateByAuthToken(token);
        this.authenticationToken = token;
        return this;
    }

    public abstract ClientRequest processPayload(JsonObject payload) throws InvalidRequestServerException;
    public abstract ClientRequestController getControllerInstance();

    public String getAction(){
        return this.action;
    }

    public User getAuthenticatedUser() {
        return authenticatedUser;
    }

    public String getAuthenticationToken(){
        return this.authenticationToken;
    }
}
