package pigeon.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import pigeon.Main;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class AboutController extends Controller implements Initializable {
    protected static FXMLLoader loader;
    private static Stage stage;

    public static void show() {
        try{
            if ( AboutController.stage == null ){
                URL url = Objects.requireNonNull(AboutController.class.getClassLoader().getResource("views/about.fxml"));
                AboutController.loader = new FXMLLoader(url);
                Parent root = AboutController.loader.load();
                AboutController.stage = new Stage();
                AboutController.stage.setTitle("About Pigeon");
                AboutController.stage.setScene(new Scene(root, 400, 300));
                AboutController.stage.setResizable(false);
            }
            AboutController.stage.toFront();
            AboutController.stage.show();
        }catch(IOException ex){
            ex.printStackTrace();
        }
    }

    public static void hide(){
        if ( AboutController.stage != null && AboutController.stage.isShowing() ){
            AboutController.stage.hide();
        }
    }

    public static AboutController getControllerInstance(){
        return AboutController.loader == null ? null : AboutController.loader.getController();
    }

    @FXML
    private Label version;

    @Override
    public void initialize(URL url, ResourceBundle rb){
        this.version.setText("Version " + Main.VERSION);
    }
}
