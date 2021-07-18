package pigeon.services;

import pigeon.exceptions.Exception;
import pigeon.models.User;
import pigeon.support.Connector;
import pigeon.support.MessageList;
import java.io.*;

public class UserService extends Service {
    private static final String TOKEN_FILE_PATH = "storage/auth_token.txt";

    private static User activeUser = null;

    private static void ensureStorageDirectory() throws IOException {
        File tokenFile = new File(UserService.TOKEN_FILE_PATH);
        File storageDirectory = tokenFile.getParentFile();
        if ( !storageDirectory.exists() ){
            if ( !storageDirectory.mkdir() ){
                throw new IOException("Unable to create the strorage directory.");
            }
        }
    }

    public UserService loginWithToken() throws IOException, Exception {
        File tokenFile = new File(UserService.TOKEN_FILE_PATH);
        if ( tokenFile.exists() ){
            BufferedReader bufferedReader = new BufferedReader(new FileReader(tokenFile));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ( ( line = bufferedReader.readLine() ) != null ){
                stringBuilder.append(line);
            }
            String contents = stringBuilder.toString().trim();
            int index = contents.lastIndexOf(":");
            if ( index > 0 ){
                String hostname = contents.substring(0, index);
                String token = contents.substring(index + 1);
                if ( !token.isEmpty() ){
                    Connector.setHostname(hostname);
                    Connector.setToken(token);
                    Connector connector = new Connector();
                    UserService.activeUser = connector.profile();
                    if ( UserService.activeUser == null ){
                        Connector.setToken(null);
                    }
                }
            }
        }
        return this;
    }

    public UserService destroyUserSession(){
        File tokenFile = new File(UserService.TOKEN_FILE_PATH);
        if ( tokenFile.exists() ){
            tokenFile.delete();
        }
        return this;
    }

    public User login(String username, String password) throws IOException, Exception {
        String[] components = username.split("@");
        if ( components.length > 1 && !components[1].isEmpty() ){
            Connector.setHostname(components[1]);
        }
        Connector connector = new Connector();
        UserService.activeUser = connector.login(components[0], password);
        if ( UserService.activeUser != null ){
            String tokenFileContent = Connector.getHostname() + ":" + Connector.getToken();
            UserService.ensureStorageDirectory();
            File tokenFile = new File(UserService.TOKEN_FILE_PATH);
            PrintWriter printWriter = new PrintWriter(tokenFile);
            printWriter.println(tokenFileContent);
            printWriter.close();
        }
        return UserService.activeUser;
    }

    public User getActiveUser() throws IOException, Exception {
        if ( UserService.activeUser == null ){
            this.loginWithToken();
        }
        return UserService.activeUser;
    }

    public boolean isUserLoggedIn(boolean tryLogin) throws IOException, Exception {
        return tryLogin ? ( this.getActiveUser() != null ) : ( UserService.activeUser != null );
    }

    public UserService logout() throws IOException, Exception {
        MessageList.getList(MessageList.MODE_RECEIVED).stopListener();
        Connector connector = new Connector();
        connector.logout();
        Connector.setHostname(null);
        Connector.setToken(null);
        this.destroyUserSession();
        return this;
    }
}
