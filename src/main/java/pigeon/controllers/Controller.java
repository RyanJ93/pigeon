package pigeon.controllers;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public abstract class Controller {

    protected static Alert makeAlert(String title, String content){
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText("");
        alert.setContentText(content);
        return alert;
    }

    protected static Alert makeConnectionIssueAlert(String title){
        String content = "You apparently are offline or the server you are trying to contact is offline, please check your connection and the server status.";
        return Controller.makeAlert(title, content);
    }

    protected static Alert makeConfirmation(String title, String content){
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText("");
        alert.setContentText(content);
        return alert;
    }
}
