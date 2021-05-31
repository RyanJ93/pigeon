package pigeonServer.models.server;

import pigeonServer.services.StorageService;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class User implements Serializable {
    public static final String SYSTEM_USER_UUID = "00000000-0000-0000-0000-000000000000";
    public static final String SYSTEM_USER_USERNAME = "system";

    private static User systemUser;

    private static String generatePasswordHash(String password) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-512");
        messageDigest.update(StandardCharsets.UTF_8.encode(password));
        return String.format("%032x", new BigInteger(1, messageDigest.digest()));
    }

    public static User getSystemUser(){
        if ( User.systemUser == null ){
            User.systemUser = new User();
            User.systemUser.setProperties(new HashMap<>(Map.of(
                "id", User.SYSTEM_USER_UUID,
                "username", User.SYSTEM_USER_USERNAME,
                "password", ""
            )));
        }
        return User.systemUser;
    }

    public static User find(String id) throws IOException {
        User user = null;
        StorageService storageService = new StorageService();
        HashMap<String, String> document = storageService.setEntity("users").findOne(id);
        if ( document != null ){
            user = new User();
            user.setProperties(document);
        }else if ( id.equals(User.SYSTEM_USER_UUID) ){
            user = User.getSystemUser();
        }
        return user;
    }

    public static User findByUsername(String username) throws IOException {
        User user = null;
        StorageService storageService = new StorageService();
        HashMap<String, String> document = storageService.setEntity("users_by_username").findOne(username);
        if ( document != null ){
            user = new User();
            user.setProperties(document);
        }else if ( username.equals(User.SYSTEM_USER_USERNAME) ){
            user = User.getSystemUser();
        }
        return user;
    }

    private String id;
    private String username;
    private transient String password;
    private transient boolean bound = false;

    public User(){
        this.id = UUID.randomUUID().toString();
    }

    public String getID(){
        return this.id;
    }

    public User setUsername(String username){
        this.username = username;
        return this;
    }

    public String getUsername(){
        return this.username;
    }

    public User setPassword(String password){
        this.password = password;
        return this;
    }

    public String getPassword(){
        return this.password;
    }

    public boolean passwordCompare(String passwordToCompare) throws NoSuchAlgorithmException {
        return this.password.equals(User.generatePasswordHash(passwordToCompare));
    }

    public boolean isBound(){
        return this.bound;
    }

    public User setProperties(HashMap<String, String> properties){
        this.id = properties.get("id");
        this.username = properties.get("username");
        this.password = properties.get("password");
        this.bound = true;
        return this;
    }

    public HashMap<String, String> getProperties() throws NoSuchAlgorithmException {
        HashMap<String, String> properties = new HashMap<>();
        properties.put("id", this.id);
        properties.put("username", this.username);
        properties.put("password", User.generatePasswordHash(this.password));
        return properties;
    }

    public User save() throws IOException, NoSuchAlgorithmException {
        HashMap<String, String> properties = this.getProperties();
        StorageService storageService = new StorageService();
        if ( this.bound ){
            storageService.setEntity("users").update(this.id, properties, true);
            storageService.setEntity("users_by_username").update(this.username, properties, true);
        }else{
            storageService.setEntity("users").insert(this.id, properties);
            storageService.setEntity("users_by_username").insert(this.username, properties);
            this.bound = true;
        }
        return this;
    }

    public User delete() throws IOException {
        StorageService storageService = new StorageService();
        storageService.setEntity("users").deleteOne(this.id);
        storageService.setEntity("users_by_username").deleteOne(this.username);
        return this;
    }
}
