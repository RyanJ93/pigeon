package pigeonServer.exceptions;

public class ServerException extends Exception {
    public ServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServerException(String message){
        super(message);
    }

    public String getResponseMessage(){
        return "An error occurred, please retry later.";
    }

    public int getResponseCode(){
        return 500;
    }
}
