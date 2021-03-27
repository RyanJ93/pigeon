package pigeonServer.support;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import pigeonServer.exceptions.InvalidRequestServerException;
import pigeonServer.exceptions.RequestParsingServerException;
import pigeonServer.exceptions.UnsupportedActionServerException;
import pigeonServer.models.server.clientRequest.*;

public class ClientRequestParser {
    private String action;
    private JsonObject payload;
    private JsonObject request;
    private ClientRequest clientRequest;

    private void extractRequestComponents(String requestData) throws InvalidRequestServerException {
        try{
            Gson gson = new Gson();
            this.request = gson.fromJson(requestData, JsonObject.class);
            this.action = this.request.has("action") ? this.request.get("action").getAsString() : "";
            this.payload = this.request.has("payload") ? this.request.get("payload").getAsJsonObject() : null;
        }catch(Exception ex){
            Logger.logException(ex, false);
            throw new InvalidRequestServerException("Invalid JSON syntax.");
        }
    }

    private void prepareClientRequestObject() throws RequestParsingServerException, UnsupportedActionServerException {
        try{
            this.clientRequest = null;
            switch (this.action) {
                case "login" -> {
                    this.clientRequest = new LoginClientRequest();
                    this.clientRequest.processPayload(this.payload);
                }
                case "send" -> {
                    this.clientRequest = new SendClientRequest();
                    this.clientRequest.checkAuth(this.request).processPayload(this.payload);
                }
                case "fetch" -> {
                    this.clientRequest = new FetchClientRequest();
                    this.clientRequest.checkAuth(this.request).processPayload(this.payload);
                }
                case "mark" -> {
                    this.clientRequest = new MarkClientRequest();
                    this.clientRequest.checkAuth(this.request).processPayload(this.payload);
                }
                case "delete" -> {
                    this.clientRequest = new DeleteClientRequest();
                    this.clientRequest.checkAuth(this.request).processPayload(this.payload);
                }
                case "logout" -> {
                    this.clientRequest = new LogoutClientRequest();
                    this.clientRequest.checkAuth(this.request).processPayload(this.payload);
                }
            }
        }catch(Exception ex){
            Logger.logException(ex, false);
            throw new RequestParsingServerException("An error occurred while parsing client request.", ex);
        }
        if ( this.clientRequest == null ){
            throw new UnsupportedActionServerException("Unsupported action.");
        }
    }

    public ClientRequestParser parse(String requestData) throws UnsupportedActionServerException, InvalidRequestServerException, RequestParsingServerException {
        this.extractRequestComponents(requestData);
        this.prepareClientRequestObject();
        return this;
    }

    public ClientRequest getClientRequest(){
        return this.clientRequest;
    }
}
