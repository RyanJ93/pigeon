package pigeon.exceptions;

import javafx.scene.control.Alert;

public class Exception extends java.lang.Exception {
    public String getHumanReadableTitle(){
        return "Unable to complete the action";
    }

    public String getHumanReadableMessage(){
        return "An error occurred while interacting with the server, please retry later.";
    }

    public Alert getAlert(){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(this.getHumanReadableTitle());
        alert.setHeaderText("");
        alert.setContentText(this.getHumanReadableMessage());
        return alert;
    }
}
