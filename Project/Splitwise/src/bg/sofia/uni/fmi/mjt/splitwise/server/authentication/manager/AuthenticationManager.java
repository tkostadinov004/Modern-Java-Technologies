package bg.sofia.uni.fmi.mjt.splitwise.server.authentication.manager;

import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;

public interface AuthenticationManager {
    boolean isAuthenticated(String username);

    boolean isAuthenticated(User user);

    void authenticate(String username, String password);
}
