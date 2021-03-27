package pigeonServer.support;

import pigeonServer.models.Console;

public class Logger {
    private static boolean debug = false;

    public static void setDebug(boolean debug){
        Logger.debug = debug;
    }

    public static boolean getDebug(){
        return Logger.debug;
    }

    public static void log(String message){
        Console.getActiveConsole().write(message);
    }

    public static void logException(Throwable exception, boolean showOutput){
        exception.printStackTrace();
        if ( showOutput || Logger.debug ){
            Logger.log("Server error: " + exception.getMessage());
        }
    }
}
