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

public class Main extends Application {
    private static void setupOptions(String[] args){
        if ( Arrays.asList(args).contains("--debug") ){
            Logger.setDebug(true);
        }
    }

    private void bootstrapServer(){
        Logger.log("Starting up the Pigeon server on port 2898...");
        ServerService serverService = new ServerService();
        serverService.start();
        Logger.log("Pigeon server is ready up!");
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        URL url = this.getClass().getClassLoader().getResource("views/console.fxml");
        Parent root = FXMLLoader.load(Objects.requireNonNull(url));
        primaryStage.setTitle("Pigeon server console");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();
        this.bootstrapServer();
    }

    public static void main(String[] args){
        Main.setupOptions(args);
        launch(args);
    }
}
