package pigeonServer.controllers.server;

import pigeonServer.exceptions.ControllerServerException;
import pigeonServer.models.server.clientRequest.ClientRequest;
import java.util.HashMap;

public abstract class ClientRequestController {
    protected ClientRequest clientRequest;

    public ClientRequestController(ClientRequest clientRequest){
        this.clientRequest = clientRequest;
    }

    public abstract HashMap<String, Object> handle() throws ControllerServerException;
}
