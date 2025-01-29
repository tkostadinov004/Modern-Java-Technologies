package bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator;

import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.exception.AlreadyAuthenticatedException;
import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.exception.NotAuthenticatedException;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;

public interface Authenticator {
    boolean isAuthenticated();

    User getAuthenticatedUser();

    void authenticate(String username, String password) throws AlreadyAuthenticatedException;

    void logout() throws NotAuthenticatedException;
}
