package bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.models.Notification;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.NotificationsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.NonExistingUserException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class DefaultNotificationsRepository implements NotificationsRepository {
    private final UserRepository userRepository;
    private final Map<User, Set<Notification>> notificationsMap;

    public DefaultNotificationsRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.notificationsMap = new HashMap<>();
    }

    @Override
    public Set<Notification> getNotificationForUser(String username) {
        if (username == null || username.isEmpty() || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null, blank or empty!");
        }

        Optional<User> user = userRepository.getUserByUsername(username);
        if (user.isEmpty()) {
            throw new NonExistingUserException("User with username %s does not exist!".formatted(username));
        }

        return getNotificationForUser(user.get());
    }

    @Override
    public Set<Notification> getNotificationForUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null!");
        }
        if (!notificationsMap.containsKey(user)) {
            return Set.of();
        }

        return notificationsMap.get(user);
    }

    @Override
    public void addNotificationForUser(String username, String notificationContent, LocalDateTime timeSent) {
        if (username == null || username.isEmpty() || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null, blank or empty!");
        }

        Optional<User> user = userRepository.getUserByUsername(username);
        if (user.isEmpty()) {
            throw new NonExistingUserException("User with username %s does not exist!".formatted(username));
        }

        addNotificationForUser(user.get(), notificationContent, timeSent);
    }

    @Override
    public void addNotificationForUser(User user, String notificationContent, LocalDateTime timeSent) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null!");
        }
        if (notificationContent == null || notificationContent.isEmpty() || notificationContent.isBlank()) {
            throw new IllegalArgumentException("Notification content cannot be null, blank or empty!");
        }
        if (timeSent == null) {
            throw new IllegalArgumentException("Time sent cannot be null!");
        }

        notificationsMap.putIfAbsent(user, new HashSet<>());

        Notification notification = new Notification(notificationContent, user, timeSent);
        notificationsMap.get(user).add(notification);
    }

    @Override
    public void removeAllNotificationsForUser(String username) {
        if (username == null || username.isEmpty() || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null, blank or empty!");
        }

        Optional<User> user = userRepository.getUserByUsername(username);
        if (user.isEmpty()) {
            throw new NonExistingUserException("User with username %s does not exist!".formatted(username));
        }
        removeAllNotificationsForUser(user.get());
    }

    @Override
    public void removeAllNotificationsForUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null!");
        }

        notificationsMap.remove(user);
    }
}
