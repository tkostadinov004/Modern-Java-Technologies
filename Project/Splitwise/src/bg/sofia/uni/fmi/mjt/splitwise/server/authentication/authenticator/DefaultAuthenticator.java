package bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator;

import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.exception.AlreadyAuthenticatedException;
import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.exception.InvalidCredentialsException;
import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.hash.PasswordHasher;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.NonExistingUserException;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Optional;

public class DefaultAuthenticator implements Authenticator {
    private UserRepository userRepository;
    private Socket userSocket;
    private User user;

    public DefaultAuthenticator(UserRepository userRepository, Socket userSocket) {
        this.userRepository = userRepository;
        this.userSocket = userSocket;
    }

    @Override
    public boolean isAuthenticated() {
        return user != null;
    }

    @Override
    public User getAuthenticatedUser() {
        return user;
    }

    @Override
    public void authenticate(String username, String password) throws AlreadyAuthenticatedException {
        if (username == null || username.isEmpty() || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null, blank or empty!");
        }
        if (password == null || password.isEmpty() || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null, blank or empty!");
        }
        if (isAuthenticated()) {
            throw new AlreadyAuthenticatedException("You are already logged in!");
        }

        Optional<User> user = userRepository.getUserByUsername(username);
        if (user.isEmpty()) {
            throw new NonExistingUserException("User with username %s does not exist!".formatted(username));
        }

        PasswordHasher hasher = new PasswordHasher();
        if (!hasher.hash(password).equals(user.get().hashedPass())) {
            throw new InvalidCredentialsException("Wrong password!");
        }
        this.user = user.get();
        userRepository.bindSocketToUser(username, userSocket);
    }
}
