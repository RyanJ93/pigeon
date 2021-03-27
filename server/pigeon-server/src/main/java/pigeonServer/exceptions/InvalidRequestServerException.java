package pigeonServer.exceptions;

public class InvalidRequestServerException extends ServerException {
    public InvalidRequestServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidRequestServerException(String message){
        super(message);
    }

    public String getResponseMessage(){
        return "Invalid request.";
    }

    public int getResponseCode(){
        return 400;
    }
}
