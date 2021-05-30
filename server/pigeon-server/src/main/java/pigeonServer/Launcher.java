package pigeonServer;

import javafx.application.Application;
import pigeonServer.models.server.User;
import pigeonServer.services.MessageService;
import io.sentry.Sentry;

public class Launcher {
    private static void handleUserAddMode(String[] args){
        try{
            if ( args.length < 2 || args[1].isEmpty() ){
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
            MessageService messageService = new MessageService();
            messageService.setUser(user).sendWelcomeMessage();
            System.out.println("Registered user with ID " + user.getID());
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
        Launcher.setupSentry();
        if ( args.length > 0 && args[0].equals("--useradd") ){
            Launcher.handleUserAddMode(args);
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
