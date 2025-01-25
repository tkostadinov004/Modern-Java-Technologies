package bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator;

import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;

public interface Authenticator {
    boolean isAuthenticated(String username);

    boolean isAuthenticated(User user);

    void authenticate(String username, String password);
}
