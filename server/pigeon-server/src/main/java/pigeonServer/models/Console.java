package pigeonServer.models;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableStringValue;

public class Console {
    private static Console activeConsole;
    private final StringProperty contents = new SimpleStringProperty();

    public static Console getActiveConsole(){
        if ( Console.activeConsole == null ){
            Console.activeConsole = new Console();
        }
        return Console.activeConsole;
    }

    public void write(String log){
        Platform.runLater(() -> {
            String contents = this.getContents();
            contents = contents == null ? ( log + "\n" ) : ( contents + log + "\n" );
            this.contents.set(contents);
        });
    }

    public String getContents(){
        return this.contents.get();
    }

    public ObservableStringValue getObservableValue(){
        return this.contents;
    }
}
