package pigeon.services;

import pigeon.exceptions.Exception;
import pigeon.models.Message;
import pigeon.support.Connector;
import java.io.IOException;
import java.util.ArrayList;

public class MessageService extends Service {
    public Message send(ArrayList<String> to, String subject, String body) throws IOException, Exception {
        Connector connector = new Connector();
        return connector.send(to, subject, body);
    }
}
