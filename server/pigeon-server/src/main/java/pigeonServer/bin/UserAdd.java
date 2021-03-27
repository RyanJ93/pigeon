package pigeonServer.bin;

import pigeonServer.models.server.User;

public class UserAdd {
    public static void main(String[] args){
        try{
            User user = new User();
            user.setUsername(args[0]).setPassword(args[1]).save();
            System.out.println("Registered user with ID " + user.getID());
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
}
