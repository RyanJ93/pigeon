package pigeon;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import pigeon.controllers.AboutController;
import pigeon.controllers.EditorController;
import pigeon.controllers.LoginController;
import pigeon.controllers.MainController;
import pigeon.exceptions.Exception;
import pigeon.services.UserService;
import pigeon.support.Connector;

import javax.swing.*;
import java.awt.*;
import java.awt.desktop.AboutEvent;
import java.awt.desktop.AboutHandler;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.Objects;

public class Main extends Application {
    public static final String VERSION = "0.0.1";

    public static void requestLogin(){
        Platform.runLater(() -> {
            UserService userService = new UserService();
            userService.destroyUserSession();
            Connector.setHostname(null);
            Connector.setToken(null);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Account disconnected");
            alert.setHeaderText("");
            alert.setContentText("You have been disconnected, please log back in.");
            alert.showAndWait().ifPresent(rs -> {
                AboutController.hide();
                EditorController.hide();
                MainController.hide();
                LoginController.show();
            });
        });
    }

    public static void notifyNewMessages(int count){
        Platform.runLater(() -> {System.out.println("NEW MESSAGE");
            URL url = Objects.requireNonNull(Main.class.getClassLoader().getResource("assets/notification.wav"));
            String content = "You have received " + count + " new message" + ( count == 1 ? "." : "s." );
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("You have new messages");
            alert.setHeaderText("");
            alert.setContentText(content);
            alert.show();
            final Media media = new Media(url.toString());
            final MediaPlayer mediaPlayer = new MediaPlayer(media);
            mediaPlayer.play();
        });
    }

    private void setup(){
        final String os = System.getProperty("os.name");
        if ( os != null && os.toLowerCase(Locale.ROOT).startsWith("mac") ){
            Desktop.getDesktop().setAboutHandler(e -> AboutController.show());
        }
    }

    @Override
    public void start(Stage primaryStage){
        this.setup();
        UserService userService = new UserService();
        try{
            if ( !userService.isUserLoggedIn(true) ){
                LoginController.show();
            }else{
                MainController.show();
            }
        }catch(Exception ex){
            ex.printStackTrace();
            ex.getAlert().show();
        }catch(IOException ex){
            ex.printStackTrace();
            userService.destroyUserSession();
            LoginController.show();
        }
    }
}
