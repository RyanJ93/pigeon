package pigeonServer.exceptions;

public class UserNotFoundServerException extends ServerException {
    public UserNotFoundServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserNotFoundServerException(String message){
        super(message);
    }

    public String getResponseMessage(){
        return "No such user found.";
    }

    public int getResponseCode(){
        return 400;
    }
}
