package pigeonServer.exceptions;

public class ControllerServerException extends ServerException {
    public ControllerServerException(String message, Throwable cause){
        super(message, cause);
    }

    public ControllerServerException(String message){
        super(message);
    }
}
