package bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.hash.Hasher;
import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.hash.PasswordHasher;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.AlreadyRegisteredException;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.NonExistingUserException;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class DefaultUserRepository implements UserRepository {
    private final Set<User> users;
    private final Map<String, Socket> userSockets;

    public DefaultUserRepository() {
        this.users = new HashSet<>();
        this.userSockets = new HashMap<>();
    }

    @Override
    public boolean containsUser(String username) {
        if (username == null || username.isEmpty() || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null, blank or empty!");
        }

        return getUserByUsername(username).isPresent();
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        if (username == null || username.isEmpty() || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null, blank or empty!");
        }

        return users
                .stream()
                .filter(user -> user.username().equals(username))
                .findFirst();
    }

    @Override
    public Optional<Socket> getSocketByUsername(String username) {
        if (!userSockets.containsKey(username)) {
            return Optional.empty();
        }

        return Optional.of(userSockets.get(username));
    }

    @Override
    public void bindSocketToUser(String username, Socket socket) {
        if (!containsUser(username)) {
            throw new NonExistingUserException("User with username %s does not exist!".formatted(username));
        }

        userSockets.put(username, socket);
    }

    private void validateUser(String username, String password, String firstName, String lastName) {
        if (username == null || username.isEmpty() || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null, blank or empty!");
        }
        if (password == null || password.isEmpty() || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null, blank or empty!");
        }
        if (firstName == null || firstName.isEmpty() || firstName.isBlank()) {
            throw new IllegalArgumentException("First name cannot be null, blank or empty!");
        }
        if (lastName == null || lastName.isEmpty() || lastName.isBlank()) {
            throw new IllegalArgumentException("Last name cannot be null, blank or empty!");
        }
    }

    @Override
    public void registerUser(String username, String password, String firstName, String lastName) {
        validateUser(username, password, firstName, lastName);
        if (containsUser(username)) {
            throw new AlreadyRegisteredException("A user with this username is already registered!");
        }

        Hasher hasher = new PasswordHasher();
        User user = new User(username, hasher.hash(password), firstName, lastName);
        users.add(user);
    }
}
