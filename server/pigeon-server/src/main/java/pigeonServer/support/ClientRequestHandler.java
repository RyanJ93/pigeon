package pigeonServer.support;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import pigeonServer.controllers.server.ClientRequestController;
import pigeonServer.exceptions.ControllerServerException;
import pigeonServer.exceptions.InvalidRequestServerException;
import pigeonServer.exceptions.RequestParsingServerException;
import pigeonServer.exceptions.ServerException;
import pigeonServer.models.server.clientRequest.ClientRequest;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;

public class ClientRequestHandler extends Thread {
    private final Socket incoming;
    private final String clientAddress;
    private String requestData;

    private void loadClientRequest() throws IOException, InvalidRequestServerException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.incoming.getInputStream()));
        this.requestData = new String(Base64.getDecoder().decode(bufferedReader.readLine()));
        if ( this.requestData.isEmpty() ){
            throw new InvalidRequestServerException("Empty request.");
        }
    }

    private void sendResponse(HashMap<String, Object> response){
        try{
            DataOutputStream dataOutputStream = new DataOutputStream(this.incoming.getOutputStream());
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").create();
            String encodedData = Base64.getEncoder().encodeToString(gson.toJson(response).getBytes(StandardCharsets.UTF_8));
            dataOutputStream.writeBytes(encodedData + '\n');
        }catch(IOException ex){
            ex.printStackTrace();
            Logger.log("Unable to send a response to client " + this.clientAddress);
        }
    }

    private void sendErrorResponse(int code, String message){
        HashMap<String, Object> response = new HashMap<>();
        response.put("response", "error");
        HashMap<String, Object> payload = new HashMap<>();
        payload.put("code", code);
        payload.put("message", message);
        response.put("error", payload);
        this.sendResponse(response);
    }

    private void sendSuccessResponse(Object payload){
        HashMap<String, Object> response = new HashMap<>();
        response.put("response", "success");
        if ( payload != null ){
            response.put("payload", payload);
        }
        this.sendResponse(response);
        Logger.log("Request from client " + this.clientAddress + " completed successfully.");
    }

    private void closeClientRequest(){
        try{
            this.incoming.close();
        }catch(IOException ex){
            ex.printStackTrace();
            Logger.log("Failed to close request from client " + this.clientAddress);
        }
    }

    private void handle(){
        try{
            this.loadClientRequest();
            ClientRequestParser clientRequestParser = new ClientRequestParser();
            ClientRequest clientRequest = clientRequestParser.parse(this.requestData).getClientRequest();
            ClientRequestController clientRequestController = clientRequest.getControllerInstance();
            this.sendSuccessResponse(clientRequestController.handle());
        }catch(ControllerServerException | RequestParsingServerException ex){
            if ( ex.getCause() instanceof ServerException ){
                ServerException cause = (ServerException)ex.getCause();
                this.sendErrorResponse(cause.getResponseCode(), cause.getResponseMessage());
                Logger.log("An error occurred while processing request from client " + this.clientAddress + ": " + cause.getMessage());
            }else{
                ex.printStackTrace();
                this.sendErrorResponse(500, "Unexpected error.");
                Logger.log("Failed processing request from client " + this.clientAddress);
            }
        }catch(ServerException ex){
            this.sendErrorResponse(ex.getResponseCode(), ex.getResponseMessage());
            Logger.log("An error occurred while processing request from client " + this.clientAddress + ": " + ex.getMessage());
        }catch(IOException ex){
            ex.printStackTrace();
            this.sendErrorResponse(500, "Unexpected error.");
            Logger.log("Failed request loading from client " + this.clientAddress);
        }catch(Exception ex){
            ex.printStackTrace();
            this.sendErrorResponse(500, "Unexpected error.");
            Logger.log("Failed processing request from client " + this.clientAddress);
        }finally{
            this.closeClientRequest();
        }
    }

    public ClientRequestHandler(Socket incoming){
        this.incoming = incoming;
        this.clientAddress = incoming.getInetAddress().getHostAddress();
        this.handle();
    }
}
