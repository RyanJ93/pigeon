package pigeonServer.exceptions;

public class UnauthorizedServerException extends ServerException {
    public UnauthorizedServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnauthorizedServerException(String message){
        super(message);
    }

    public String getResponseMessage(){
        return "Unauthorized";
    }

    public int getResponseCode(){
        return 403;
    }
}
