package pigeonServer.exceptions;

public class UnsupportedActionServerException extends ServerException {
    public UnsupportedActionServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnsupportedActionServerException(String message){
        super(message);
    }

    public String getResponseMessage(){
        return "Unsupported action.";
    }

    public int getResponseCode(){
        return 405;
    }
}
