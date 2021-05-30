package pigeon.support;

import com.google.gson.*;
import pigeon.exceptions.Exception;
import pigeon.exceptions.ServerException;
import pigeon.exceptions.UnauthorizedException;
import pigeon.exceptions.InvalidCredentialsException;
import pigeon.exceptions.UserNotFoundException;
import pigeon.models.Message;
import pigeon.models.User;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Connector {
    private static final int PORT = 2898;

    private static String hostname = null;
    private static String token = null;

    public static void setHostname(String hostname){
        Connector.hostname = hostname;
    }

    public static String getHostname(){
        return Connector.hostname;
    }

    public static void setToken(String token){
        Connector.token = token;
    }

    public static String getToken(){
        return Connector.token;
    }

    private JsonObject sendRequest(String action, HashMap<String, Object> payload) throws IOException {
        HashMap<String, Object> request = new HashMap<>();
        request.put("action", action);
        if ( Connector.token != null ){
            request.put("token", Connector.token);
        }
        if ( payload != null ){
            request.put("payload", payload);
        }
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        Socket socket = new Socket(Connector.hostname, Connector.PORT);
        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String encodedData = Base64.getEncoder().encodeToString(gson.toJson(request).getBytes(StandardCharsets.UTF_8));
        dataOutputStream.writeBytes(encodedData + '\n');
        String responseText = new String(Base64.getDecoder().decode(bufferedReader.readLine()));
        socket.close();
        gson = new Gson();
        return gson.fromJson(responseText, JsonObject.class);
    }

    public User profile() throws IOException, Exception {
        User user;
        JsonObject response = this.sendRequest("profile", null);
        if ( response.get("response").getAsString().equals("success") ){
            JsonObject payload = response.getAsJsonObject("payload");
            String username = payload.get("username").getAsString();
            String id = payload.get("id").getAsString();
            user = new User(id, username);
        }else{
            switch (response.get("error").getAsJsonObject().get("code").getAsInt()){
                case 404 -> throw new UserNotFoundException();
                case 403 -> throw new UnauthorizedException();
                default -> throw new ServerException();
            }
        }
        return user;
    }

    public User login(String username, String password) throws IOException, Exception {
        User user;
        HashMap<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("username", username);
        requestPayload.put("password", password);
        JsonObject response = this.sendRequest("login", requestPayload);
        if ( response.get("response").getAsString().equals("success") ){
            JsonObject payload = response.getAsJsonObject("payload");
            Connector.token = payload.get("token").getAsString();
            String id = payload.get("id").getAsString();
            user = new User(id, username);
        }else{
            switch (response.get("error").getAsJsonObject().get("code").getAsInt()){
                case 404 -> throw new UserNotFoundException();
                case 403 -> throw new InvalidCredentialsException();
                default -> throw new ServerException();
            }
        }
        return user;
    }

    public void logout() throws IOException, ServerException {
        JsonObject response = this.sendRequest("logout", null);
        if ( !response.get("response").getAsString().equals("success") ){
            throw new ServerException();
        }
    }

    public ArrayList<Message> fetch(Date start, boolean unreadOnly, boolean sent) throws IOException, Exception {
        ArrayList<Message> messages = new ArrayList<>();
        HashMap<String, Object> requestPayload = new HashMap<>();
        if ( start != null ){
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            requestPayload.put("start", dateFormat.format(start));
        }
        requestPayload.put("unreadOnly", unreadOnly);
        requestPayload.put("sent", sent);
        JsonObject response = this.sendRequest("fetch", requestPayload);
        if ( response.get("response").getAsString().equals("success") ){
            JsonObject payload = response.getAsJsonObject("payload");
            JsonArray messageList = payload.get("messages").getAsJsonArray();
            for ( JsonElement message : messageList ){
                messages.add(new Message(message.getAsJsonObject(), sent));
            }
        }else{
            switch (response.get("error").getAsJsonObject().get("code").getAsInt()){
                case 404 -> throw new UserNotFoundException();
                case 403 -> throw new UnauthorizedException();
                default -> throw new ServerException();
            }
        }
        return messages;
    }

    public Message send(ArrayList<String> to, String subject, String body) throws IOException, Exception {
        HashMap<String, Object> requestPayload = new HashMap<>();
        Message message;
        requestPayload.put("to", to.toArray());
        requestPayload.put("subject", subject);
        requestPayload.put("body", body);
        JsonObject response = this.sendRequest("send", requestPayload);
        if ( response.get("response").getAsString().equals("success") ){
            JsonObject payload = response.getAsJsonObject("payload");
            message = new Message(payload.get("message").getAsJsonObject(), true);
        }else{
            switch (response.get("error").getAsJsonObject().get("code").getAsInt()){
                case 404 -> throw new UserNotFoundException();
                case 403 -> throw new UnauthorizedException();
                default -> throw new ServerException();
            }
        }
        return message;
    }

    public void delete(String id, boolean sent) throws IOException, Exception {
        HashMap<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("messages", new String[]{id});
        requestPayload.put("sent", sent);
        JsonObject response = this.sendRequest("delete", requestPayload);
        if ( !response.get("response").getAsString().equals("success") ){
            switch (response.get("error").getAsJsonObject().get("code").getAsInt()){
                case 404 -> throw new UserNotFoundException();
                case 403 -> throw new UnauthorizedException();
                default -> throw new ServerException();
            }
        }
    }

    public void mark(boolean read, String[] ids) throws IOException, Exception {
        HashMap<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("messages", ids);
        requestPayload.put("read", read);
        JsonObject response = this.sendRequest("mark", requestPayload);
        if ( !response.get("response").getAsString().equals("success") ){
            switch (response.get("error").getAsJsonObject().get("code").getAsInt()){
                case 404 -> throw new UserNotFoundException();
                case 403 -> throw new UnauthorizedException();
                default -> throw new ServerException();
            }
        }
    }
}
