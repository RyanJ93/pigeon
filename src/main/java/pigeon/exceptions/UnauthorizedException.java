package pigeon.exceptions;

public class UnauthorizedException extends Exception {
    @Override
    public String getHumanReadableTitle(){
        return "Invalid credentials";
    }

    @Override
    public String getHumanReadableMessage(){
        return "Provided credentials are not valid.";
    }
}
