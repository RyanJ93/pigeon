package pigeonServer.models.server.clientRequest;

import com.google.gson.JsonObject;
import pigeonServer.controllers.server.LogoutClientRequestController;
import pigeonServer.exceptions.InvalidRequestServerException;

public class LogoutClientRequest extends ClientRequest {
    public LogoutClientRequest(){
        this.action = "logout";
    }

    public LogoutClientRequest processPayload(JsonObject payload) throws InvalidRequestServerException {
        return this;
    }

    public LogoutClientRequestController getControllerInstance(){
        return new LogoutClientRequestController(this);
    }
}
