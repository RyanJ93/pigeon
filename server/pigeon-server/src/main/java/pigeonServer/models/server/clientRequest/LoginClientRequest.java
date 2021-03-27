package pigeonServer.models.server.clientRequest;

import com.google.gson.JsonObject;
import pigeonServer.controllers.server.LoginClientRequestController;
import pigeonServer.exceptions.InvalidRequestServerException;

public class LoginClientRequest extends ClientRequest {
    private String username = null;
    private String password = null;

    public LoginClientRequest(){
        this.action = "login";
    }

    public LoginClientRequest processPayload(JsonObject payload) throws InvalidRequestServerException {
        if ( payload == null ){
            throw new InvalidRequestServerException("Invalid request.");
        }
        String username = payload.has("username") ? payload.get("username").getAsString() : "";
        String password = payload.has("password") ? payload.get("password").getAsString() : "";
        if ( username.isEmpty() || password.isEmpty() ){
            throw new InvalidRequestServerException("No valid credentials provided.");
        }
        this.username = username;
        this.password = password;
        return this;
    }

    public LoginClientRequestController getControllerInstance(){
        return new LoginClientRequestController(this);
    }

    public String getUsername(){
        return this.username;
    }

    public String getPassword(){
        return this.password;
    }
}
