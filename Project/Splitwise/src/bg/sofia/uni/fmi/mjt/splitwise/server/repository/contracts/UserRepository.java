package bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts;

import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;

import java.util.Optional;

public interface UserRepository {
    boolean containsUser(String username);

    Optional<User> getUserByUsername(String username);

    void registerUser(String username, String password, String firstName, String lastName);
}
