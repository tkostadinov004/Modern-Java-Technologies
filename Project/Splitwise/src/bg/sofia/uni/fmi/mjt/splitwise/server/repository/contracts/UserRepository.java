package bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts;

import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;

import java.net.Socket;
import java.util.Optional;
import java.util.Set;

public interface UserRepository {
    Set<User> getAllUsers();

    boolean containsUser(String username);

    Optional<User> getUserByUsername(String username);

    Optional<Socket> getSocketByUsername(String username);

    void bindSocketToUser(String username, Socket socket);

    void registerUser(String username, String password, String firstName, String lastName);
}
