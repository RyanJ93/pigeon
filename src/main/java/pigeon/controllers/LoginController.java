package pigeon.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import pigeon.exceptions.Exception;
import pigeon.models.User;
import pigeon.services.UserService;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginController extends Controller implements Initializable {
    protected static FXMLLoader loader;
    private static Stage stage;

    public static void show(){
        try{
            if ( LoginController.stage == null ){
                URL url = Objects.requireNonNull(LoginController.class.getClassLoader().getResource("views/login.fxml"));
                LoginController.loader = new FXMLLoader(url);
                Parent root = LoginController.loader.load();
                LoginController.stage = new Stage();
                LoginController.stage.setTitle("Sign-in with your account");
                LoginController.stage.setScene(new Scene(root, 350, 418));
                LoginController.stage.setResizable(false);
            }
            LoginController.stage.show();
        }catch(IOException ex){
            ex.printStackTrace();
        }
    }

    public static void hide(){
        if ( LoginController.stage != null && LoginController.stage.isShowing() ){
            LoginController.stage.hide();
        }
    }

    public static LoginController getControllerInstance(){
        return LoginController.loader == null ? null : LoginController.loader.getController();
    }

    @FXML
    private TextField username;

    @FXML
    private TextField password;

    @FXML
    private void handleSignInAction(){
        this.login();
    }

    private boolean validate(){
        Pattern usernamePattern = Pattern.compile("^[a-zA-Z0-9][a-zA-Z0-9._-]{0,31}$");
        Pattern hostnamePattern = Pattern.compile("^[a-zA-Z0-9._-]+\\.[a-zA-Z0-9]+$");
        String username = this.username.getText();
        String messages = "";
        if ( username.isEmpty() ){
            messages += "You must provide a valid username.\n";
        }
        String[] components = username.split("@");
        if ( components.length != 2 ){
            messages += "You must provide a valid username.\n";
        }else{
            Matcher usernameMatcher = usernamePattern.matcher(components[0]);
            Matcher hostnameMatcher = hostnamePattern.matcher(components[1]);
            if ( !usernameMatcher.matches() || !hostnameMatcher.matches() || components[0].equalsIgnoreCase("system") ){
                messages += "You must provide a valid username.\n";
            }
        }
        if ( this.password.getText().isEmpty() ){
            messages += "You must provide a valid password.\n";
        }
        if ( !messages.isEmpty() ){
            LoginController.makeAlert("Invalid credentials", messages.trim()).show();
        }
        return messages.isEmpty();
    }

    private void login(){
        try{
            if ( this.validate() ){
                UserService userService = new UserService();
                User user = userService.login(this.username.getText(), this.password.getText());
                if ( user != null ){
                    LoginController.hide();
                    MainController.show();
                    this.reset();
                }
            }
        }catch(Exception ex){
            ex.printStackTrace();
            ex.getAlert().show();
        }catch(ConnectException ex){
            ex.printStackTrace();
            LoginController.makeConnectionIssueAlert("Unable to login").show();
        }catch(IOException ex){
            ex.printStackTrace();
            LoginController.makeAlert("Unable to login", "An error occurred while logging you in, please retry later").show();
        }
    }

    private void reset(){
        this.username.setText("");
        this.password.setText("");
    }

    @Override
    public void initialize(URL url, ResourceBundle rb){}
}
