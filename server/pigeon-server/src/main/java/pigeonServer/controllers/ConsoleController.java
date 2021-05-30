package pigeonServer.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import pigeonServer.models.Console;

public class ConsoleController {
    @FXML
    private TextArea consoleOutput;
    private final Console console;

    public ConsoleController(){
        this.console = Console.getActiveConsole();
        this.console.getObservableValue().addListener((obs, oldValue, newValue) -> {
            this.consoleOutput.setText(this.console.getContents());
            this.consoleOutput.setScrollTop(Double.MAX_VALUE);
        });
    }
}
