package pigeonServer;

import javafx.application.Application;
import pigeonServer.models.server.User;
import io.sentry.Sentry;
import pigeonServer.services.NotificationService;
import java.util.regex.*;

public class Launcher {
    private static boolean validateUsername(String username){
        Pattern pattern = Pattern.compile("^[a-zA-Z0-9][a-zA-Z0-9._-]{0,31}$");
        Matcher matcher = pattern.matcher(username);
        return matcher.matches();
    }

    private static void handleUserAddMode(String[] args){
        try{
            if ( args.length < 2 || args[1].isEmpty() || !Launcher.validateUsername(args[1]) ){
                System.out.println("Invalid username.");
                System.exit(1);
            }
            if ( args.length < 3 || args[2].isEmpty() ){
                System.out.println("Invalid password.");
                System.exit(1);
            }
            if ( args[1].equalsIgnoreCase(User.SYSTEM_USER_USERNAME) ){
                System.out.println("You cannot use system as username as it is a reserved name.");
                System.exit(1);
            }
            User user = new User();
            user.setUsername(args[1]).setPassword(args[2]).save();
            NotificationService notificationService = new NotificationService();
            notificationService.setUser(user).sendWelcomeNotification();
            System.out.println("Registered user with ID " + user.getID());
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    private static void handleChangePasswordMode(String[] args){
        try{
            if ( args.length < 2 || args[1].isEmpty() ){
                System.out.println("No such user found.");
                System.exit(1);
            }
            if ( args.length < 3 || args[2].isEmpty() ){
                System.out.println("Invalid password.");
                System.exit(1);
            }
            User user = User.findByUsername(args[1]);
            if ( user == null ){
                System.out.println("No such user found.");
            }
            user.setPassword(args[2]).save();
            NotificationService notificationService = new NotificationService();
            notificationService.setUser(user).sendPasswordChangedNotification();
            System.out.println("Password changed!");
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    private static void setupSentry(){
        String sentryDSN = System.getenv("PIGEON_SENTRY_DSN");
        if ( sentryDSN != null && !sentryDSN.isEmpty() ){
            Sentry.init(options -> {
                options.setDsn(sentryDSN);
            });
            System.out.println("Sentry initialized!");
        }
    }

    public static void main(String[] args){

        try{
            NotificationService notificationService = new NotificationService();
            notificationService.setUser(User.findByUsername("test2")).sendWelcomeNotification();
        }catch (Exception ignored){}

        Launcher.setupSentry();
        if ( args.length > 0 && args[0].equals("--useradd") ){
            Launcher.handleUserAddMode(args);
        }else if ( args.length > 0 && args[0].equals("--change-password") ){
            Launcher.handleChangePasswordMode(args);
        }else{
            Main.setupOptions(args);
            if ( Main.isIsCLIMode() ){
                Main.startNoGui();
            }else{
                Application.launch(Main.class, args);



            }
        }
    }
}
