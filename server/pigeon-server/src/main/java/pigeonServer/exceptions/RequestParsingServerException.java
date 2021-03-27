package pigeonServer.exceptions;

public class RequestParsingServerException extends ServerException {
    public RequestParsingServerException(String message, Throwable cause){
        super(message, cause);
    }

    public RequestParsingServerException(String message){
        super(message);
    }
}
