package pigeon.util;

import java.util.ArrayList;

public class MessageUtils {
    public static ArrayList<String> getRecipientsFromString(String recipientList){
        ArrayList<String> recipients = new ArrayList<>();
        if ( recipientList != null && !recipientList.isEmpty() ){
            String[] recipientAddresses = recipientList.replaceAll(" ", "").split(",");
            for ( String recipientAddress : recipientAddresses ){
                String[] components = recipientAddress.split("@");
                recipients.add(components[0]);
            }
        }
        return recipients;
    }
}
