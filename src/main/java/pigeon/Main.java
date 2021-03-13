package pigeon;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import pigeon.services.UserService;

import java.net.URL;
import java.util.Objects;

public class Main extends Application {
    private void showLoginWindow() throws Exception{
        URL url = Objects.requireNonNull(getClass().getClassLoader().getResource("views/login.fxml"));
        Parent root = FXMLLoader.load(url);
        Stage stage = new Stage();
        stage.setTitle("Sign-in with your account");
        stage.setScene(new Scene(root, 350, 300));
        stage.show();
        stage.setAlwaysOnTop(true);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        UserService userService = new UserService();
        if ( !userService.isUserLoggedIn() ){
            this.showLoginWindow();
        }




        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("views/sample.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
