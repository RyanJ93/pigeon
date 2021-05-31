package pigeonServer.services;

import pigeonServer.exceptions.UnauthorizedServerException;
import pigeonServer.exceptions.UserNotFoundServerException;
import pigeonServer.models.server.AuthToken;
import pigeonServer.models.server.User;
import pigeonServer.support.Logger;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class UserService extends Service {
    public AuthToken authenticateByCredentials(String username, String password) throws UserNotFoundServerException, UnauthorizedServerException, NoSuchAlgorithmException, IOException {
        User user = User.findByUsername(username);
        if ( user == null ){
            Logger.log("Authentication attempt from user \"" + username + "\" failed: no such user found.");
            throw new UserNotFoundServerException("No such user found.");
        }
        if ( !user.passwordCompare(password) ){
            Logger.log("Authentication attempt from user \"" + username + "\" failed: password mismatch.");
            throw new UnauthorizedServerException("Password mismatch.");
        }
        AuthToken authToken = new AuthToken();
        authToken.setUserID(user.getID()).save();
        return authToken;
    }

    public User authenticateByAuthToken(String token) throws UserNotFoundServerException, UnauthorizedServerException, IOException {
        AuthToken authToken = AuthToken.find(token);
        if ( authToken == null ){
            Logger.log("Authentication attempt with token failed: no such auth token found.");
            throw new UnauthorizedServerException("No such auth token found.");
        }
        User user = User.find(authToken.getUserID());
        if ( user == null ){
            Logger.log("Authentication attempt with token failed: no such user found.");
            throw new UserNotFoundServerException("No such user found.");
        }
        return user;
    }
}
