package pigeonServer.models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableStringValue;

public class Console {
    private static final Console activeConsole = new Console();
    private final StringProperty contents = new SimpleStringProperty();

    public static Console getActiveConsole(){
        return Console.activeConsole;
    }

    public Console write(String log){
        String contents = this.getContents();
        contents = contents == null ? ( log + "\n" ) : ( contents + log + "\n" );
        this.contents.set(contents);
        return this;
    }

    public String getContents(){
        return this.contents.get();
    }

    public ObservableStringValue getObservableValue(){
        return this.contents;
    }
}
