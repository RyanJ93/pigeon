package pigeonServer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import pigeonServer.services.ServerService;
import pigeonServer.support.Logger;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;
import java.util.List;

public class Main extends Application {
    public static final String VERSION = "0.0.1";

    private static boolean CLIMode = false;

    private static void bootstrapServer(){
        Logger.log("Starting up Pigeon server version " + Main.VERSION + " on port 2898...");
        ServerService serverService = new ServerService();
        serverService.start();
        Logger.log("Pigeon server is ready up!");
    }

    public static boolean isIsCLIMode(){
        return Main.CLIMode;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        if ( !Main.CLIMode ){
            URL url = this.getClass().getClassLoader().getResource("views/console.fxml");
            Parent root = FXMLLoader.load(Objects.requireNonNull(url));
            primaryStage.setTitle("Pigeon server console");
            primaryStage.setScene(new Scene(root, 900, 500));
            primaryStage.setResizable(false);
            primaryStage.show();
        }
        Main.bootstrapServer();
    }

    public static void startNoGui(){
        Main.bootstrapServer();
    }

    public static void setupOptions(String[] args){
        List<String> arguments = Arrays.asList(args);
        if ( arguments.contains("--debug") ){
            Logger.setDebug(true);
        }
        if ( arguments.contains("--cli") || arguments.contains("--no-gui") ){
            Main.CLIMode = true;
        }
    }
}
