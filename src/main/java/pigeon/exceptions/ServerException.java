package pigeon.exceptions;

public class ServerException extends Exception {
    @Override
    public String getHumanReadableTitle(){
        return "Server error";
    }

    @Override
    public String getHumanReadableMessage(){
        return "An error occurred server-side, please retry later.";
    }
}
