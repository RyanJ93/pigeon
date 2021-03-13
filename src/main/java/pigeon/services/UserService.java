package pigeon.services;

import pigeon.models.User;

public class UserService extends Service {
    private User activeUser = null;

    public void loginWithToken(){

    }

    public User getActiveUser(){
        if ( this.activeUser == null ){
            this.loginWithToken();
        }
        return this.activeUser;
    }

    public boolean isUserLoggedIn(){
        return this.getActiveUser() != null;
    }
}
