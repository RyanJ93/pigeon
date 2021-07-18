package pigeon.support;

import pigeon.Main;
import pigeon.controllers.MainController;
import pigeon.exceptions.Exception;
import pigeon.exceptions.UnauthorizedException;
import pigeon.exceptions.UserNotFoundException;
import pigeon.models.Message;
import pigeon.services.UserService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class MessageListener extends Thread {
    private static final int MESSAGE_FETCH_INTERVAL = 3000;

    private static boolean isOnline = false;

    public static synchronized boolean isOnline(){
        return MessageListener.isOnline;
    }

    private final MessageList messageList;
    private Date lastFetchDate;

    private void fetchNewMessages(boolean announceNewMessages) throws IOException, Exception {
        ArrayList<Message> messages = Message.getReceived(this.lastFetchDate, true);
        if ( messages != null ){
            int count = 0;
            for ( Message message : messages ){
                if ( this.messageList.addMessage(message) ){
                    count++;
                }
            }
            if ( count > 0 && announceNewMessages ){
                Main.notifyNewMessages(count);
            }
        }
        this.lastFetchDate = new Date();
    }

    public MessageListener(MessageList messageList){
        this.messageList = messageList;
    }

    public void run(){
        UserService userService = new UserService();
        boolean announceNewMessages = false;
        for( ; ; ){
            try{
                if ( userService.isUserLoggedIn(false) ){
                    this.fetchNewMessages(announceNewMessages);
                    synchronized(MessageListener.class){
                        MessageListener.isOnline = true;
                    }
                    MainController.setOffline(false);
                    announceNewMessages = true;
                }
                Thread.sleep(MessageListener.MESSAGE_FETCH_INTERVAL);
            }catch(UnauthorizedException | UserNotFoundException ex){
                Main.requestLogin();
            }catch(IOException ex){
                MainController.setOffline(true);
                synchronized(MessageListener.class){
                    MessageListener.isOnline = false;
                }
            }catch(InterruptedException ex){
                break;
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
    }
}
