package pigeonServer.models.server;

import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public abstract class Message implements Serializable {
    protected String id;
    protected User sender;
    protected ArrayList<User> recipients;
    protected transient User targetUser;
    protected String subject;
    protected String body;
    protected Date date = new Date();
    protected boolean read = false;
    protected transient boolean bound = false;

    public Message(){
        this.id = UUID.randomUUID().toString();
    }

    public String getID(){
        return this.id;
    }

    public Message setSender(User sender){
        this.sender = sender;
        return this;
    }

    public User getSender(){
        return this.sender;
    }

    public Message setRecipients(ArrayList<User> recipients){
        this.recipients = recipients;
        return this;
    }

    public ArrayList<User> getRecipients(){
        return this.recipients;
    }

    public Message setTargetUser(User targetUser){
        this.targetUser = targetUser;
        return this;
    }

    public User getTargetUser(){
        return this.targetUser;
    }

    public Message setSubject(String subject){
        this.subject = subject;
        return this;
    }

    public String getSubject(){
        return this.subject;
    }

    public Message setBody(String body){
        this.body = body;
        return this;
    }

    public String getBody(){
        return this.body;
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

    public boolean isBound(){
        return this.bound;
    }

    public Message setProperties(HashMap<String, String> properties) throws IOException {
        this.id = properties.get("id");
        this.sender = User.find(properties.get("sender"));
        String[] recipientIDs = properties.get("recipients").split(",");
        this.recipients = new ArrayList<>();
        for ( String recipientID : recipientIDs ){
            if ( !recipientID.isEmpty() ){
                User recipient = User.find(recipientID);
                if ( recipient != null ){
                    this.recipients.add(recipient);
                }
            }
        }
        this.targetUser = User.find(properties.get("targetUser"));
        this.subject = properties.get("subject");
        this.body = properties.get("body");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try{
            this.date = dateFormat.parse(properties.get("date"));
        }catch(ParseException ex){
            this.date = null;
        }
        this.read = properties.get("read").equals("1");
        this.bound = true;
        return this;
    }

    public HashMap<String, String> getProperties(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        HashMap<String, String> properties = new HashMap<>();
        properties.put("id", this.id);
        properties.put("sender", this.sender.getID());
        String[] recipientIDs = new String[this.recipients.size()];
        for ( int i = 0 ; i < this.recipients.size() ; i++ ){
            recipientIDs[i] = this.recipients.get(i).getID();
        }
        properties.put("recipients", String.join(",", recipientIDs));
        properties.put("targetUser", this.targetUser.getID());
        properties.put("subject", this.subject);
        properties.put("body", this.body);
        properties.put("date", dateFormat.format(this.date));
        properties.put("read", ( this.read ? "1" : "0" ));
        return properties;
    }

    public abstract Message save() throws IOException;
    public abstract Message delete() throws IOException;
}
