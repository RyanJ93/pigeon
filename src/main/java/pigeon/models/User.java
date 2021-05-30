package pigeon.models;

import com.google.gson.JsonObject;

public class User extends Model {
    private String id;
    private String username;

    private void getPropertiesFromJsonObject(JsonObject properties){
        this.id = properties.get("id").getAsString();
        this.username = properties.get("username").getAsString();
    }

    public User(String id, String username){
        this.id = id;
        this.username = username;
    }

    public User(JsonObject properties){
        if ( properties != null ){
            this.getPropertiesFromJsonObject(properties);
        }
    }

    public String getID(){
        return this.id;
    }

    public String getUsername(){
        return this.username;
    }
}
