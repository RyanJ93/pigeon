package pigeonServer.models.server;

import pigeonServer.services.StorageService;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.HashMap;

public class AuthToken {
    private static final String TOKEN_PATTERN = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    public static final int TOKEN_LENGTH = 256;

    private static String generateToken(){
        SecureRandom secureRandom = new SecureRandom();
        StringBuilder stringBuilder = new StringBuilder();
        for ( int i = 0 ; i < AuthToken.TOKEN_LENGTH ; i++ ){
            stringBuilder.append(AuthToken.TOKEN_PATTERN.charAt(secureRandom.nextInt(AuthToken.TOKEN_PATTERN.length())));
        }
        return stringBuilder.toString();
    }

    public static AuthToken find(String token) throws IOException {
        AuthToken authToken = null;
        StorageService storageService = new StorageService();
        HashMap<String, String> document = storageService.setEntity("auth_tokens").findOne(token);
        if ( document != null ){
            authToken = new AuthToken();
            authToken.setProperties(document);
        }
        return authToken;
    }

    private String token;
    private String userID;
    private boolean bound = false;

    public AuthToken(){
        this.token = AuthToken.generateToken();
    }

    public String getToken(){
        return this.token;
    }

    public AuthToken setUserID(String userID){
        this.userID = userID;
        return this;
    }

    public String getUserID(){
        return this.userID;
    }

    public boolean isBound(){
        return this.bound;
    }

    public AuthToken setProperties(HashMap<String, String> properties){
        this.token = properties.get("token");
        this.userID = properties.get("userID");
        this.bound = true;
        return this;
    }

    public HashMap<String, String> getProperties(){
        HashMap<String, String> properties = new HashMap<>();
        properties.put("token", this.token);
        properties.put("userID", this.userID);
        return properties;
    }

    public AuthToken save() throws IOException {
        HashMap<String, String> properties = this.getProperties();
        StorageService storageService = new StorageService();
        storageService.setEntity("auth_tokens").insert(this.token, properties);
        this.bound = true;
        return this;
    }

    public AuthToken delete() throws IOException {
        StorageService storageService = new StorageService();
        storageService.setEntity("auth_tokens").deleteOne(this.token);
        this.bound = false;
        return this;
    }
}
