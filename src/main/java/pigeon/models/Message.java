package pigeon.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jsoup.Jsoup;
import pigeon.exceptions.Exception;
import pigeon.services.UserService;
import pigeon.support.Connector;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class Message extends Model {
    public static ArrayList<Message> getReceived(Date start, boolean unreadOnly) throws IOException, Exception {
        Connector connector = new Connector();
        return connector.fetch(start, unreadOnly, false);
    }

    public static ArrayList<Message> getSent(Date start, boolean unreadOnly) throws IOException, Exception {
        Connector connector = new Connector();
        return connector.fetch(start, unreadOnly, true);
    }

    private String id;
    private User sender;
    private ArrayList<User> recipients;
    private String subject;
    private String body;
    private String preview;
    private Date date = new Date();
    private boolean read = false;
    private boolean sent = false;

    private void getPropertiesFromJsonObject(JsonObject properties){
        JsonArray recipientList = properties.get("recipients").getAsJsonArray();
        this.id = properties.get("id").getAsString();
        this.sender = new User(properties.get("sender").getAsJsonObject());
        this.recipients = new ArrayList<>();
        for ( JsonElement recipient : recipientList ){
            this.recipients.add(new User(recipient.getAsJsonObject()));
        }
        this.subject = properties.get("subject").getAsString();
        this.body = properties.get("body").getAsString();
        this.preview = Jsoup.parse(this.body).text();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try{
            this.date = dateFormat.parse(properties.get("date").getAsString());
        }catch(ParseException ex){
            this.date = null;
        }
        this.read = properties.get("read").getAsBoolean();
    }

    public Message(JsonObject properties, boolean sent){
        if ( properties != null ){
            this.getPropertiesFromJsonObject(properties);
            this.sent = sent;
        }
    }

    public Message(ArrayList<User> recipients, String subject, String body, boolean sent) throws IOException, Exception {
        this.id = null;
        UserService userService = new UserService();
        this.sender = userService.getActiveUser();
        this.recipients = recipients;
        this.subject = subject;
        this.body = body;
        this.preview = Jsoup.parse(this.body).text();
        this.date = new Date();
        this.read = false;
        this.sent = sent;
    }

    public String getID(){
        return this.id;
    }

    public User getSender(){
        return this.sender;
    }

    public ArrayList<User> getRecipients(){
        return this.recipients;
    }

    public String getSubject(){
        return this.subject;
    }

    public String getBody(){
        return this.body;
    }

    public String getPreview(){
        return this.preview;
    }

    public Date getDate(){
        return this.date;
    }

    public Message setRead(boolean read){
        this.read = read;
        return this;
    }

    public boolean getRead(){
        return this.read;
    }

    public boolean getSent(){
        return this.sent;
    }
}
