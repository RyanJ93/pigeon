package pigeonServer.models.server.clientRequest;

import pigeonServer.controllers.server.FetchClientRequestController;
import pigeonServer.controllers.server.ProfileClientRequestController;
import pigeonServer.exceptions.InvalidRequestServerException;
import com.google.gson.JsonObject;

public class ProfileClientRequest extends ClientRequest {
    public ProfileClientRequest(){
        this.action = "profile";
    }

    public ProfileClientRequest processPayload(JsonObject payload) throws InvalidRequestServerException {
        return this;
    }

    public ProfileClientRequestController getControllerInstance(){
        return new ProfileClientRequestController(this);
    }
}
