package bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator;

import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.exception.AlreadyAuthenticatedException;
import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.exception.InvalidCredentialsException;
import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.exception.NotAuthenticatedException;
import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.hash.PasswordHasher;
import bg.sofia.uni.fmi.mjt.splitwise.server.dependency.DependencyContainer;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserRepository;

import java.net.Socket;
import java.util.Optional;
import java.util.logging.Logger;

public class DefaultAuthenticator implements Authenticator {
    private final Logger logger;
    private final UserRepository userRepository;
    private final Socket userSocket;
    private User user;

    public DefaultAuthenticator(DependencyContainer dependencyContainer, Socket userSocket) {
        this.logger = dependencyContainer.get(Logger.class);
        this.userRepository = dependencyContainer.get(UserRepository.class);
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
        PasswordHasher hasher = new PasswordHasher();
        if (user.isEmpty() || !hasher.hash(password).equals(user.get().hashedPass())) {
            throw new InvalidCredentialsException("Wrong username or password!");
        }
        this.user = user.get();
        userRepository.bindSocketToUser(username, userSocket);
        logger.info("User %s (%s) logged in.".formatted(username, userSocket.getInetAddress()));
    }

    @Override
    public void logout() throws NotAuthenticatedException {
        if (!isAuthenticated()) {
            throw new NotAuthenticatedException("You have to login first in order to log out!");
        }

        logger.info("User %s (%s) logged out.".formatted(user.username(), userSocket.getInetAddress()));
        this.user = null;
    }
}
