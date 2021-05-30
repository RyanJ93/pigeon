package pigeon.exceptions;

public class UserNotFoundException extends Exception {
    @Override
    public String getHumanReadableTitle(){
        return "User not found";
    }

    @Override
    public String getHumanReadableMessage(){
        return "The provided username cannot be found on the selected server.";
    }
}
