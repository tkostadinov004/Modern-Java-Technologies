package bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.data.CsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.Notification;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.NotificationType;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.dto.FriendshipRelationDTO;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.NotificationsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.NonExistingUserException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class DefaultNotificationsRepository implements NotificationsRepository {
    private final CsvProcessor<Notification> csvProcessor;
    private final UserRepository userRepository;
    private final Map<User, Set<Notification>> notificationsMap;

    private Map<User, Set<Notification>> populateNotificationsMap() {
        Set<Notification> notifications = csvProcessor.readAll();
        return notifications
                .stream()
                .collect(Collectors.groupingBy(not -> not.receiver(), Collectors.mapping(rel -> rel, Collectors.toSet())));
    }

    public DefaultNotificationsRepository(CsvProcessor<Notification> csvProcessor, UserRepository userRepository) {
        this.csvProcessor = csvProcessor;
        this.userRepository = userRepository;
        this.notificationsMap = populateNotificationsMap();
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

        if (!notificationsMap.containsKey(user.get())) {
            return Set.of();
        }
        return notificationsMap.get(user.get());
    }

    @Override
    public void addNotificationForUser(String username, String notificationContent, LocalDateTime timeSent, NotificationType type) {
        if (username == null || username.isEmpty() || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null, blank or empty!");
        }
        if (notificationContent == null || notificationContent.isEmpty() || notificationContent.isBlank()) {
            throw new IllegalArgumentException("Notification content cannot be null, blank or empty!");
        }
        if (timeSent == null) {
            throw new IllegalArgumentException("Time sent cannot be null!");
        }

        Optional<User> user = userRepository.getUserByUsername(username);
        if (user.isEmpty()) {
            throw new NonExistingUserException("User with username %s does not exist!".formatted(username));
        }

        notificationsMap.putIfAbsent(user.get(), new LinkedHashSet<>());

        Notification notification = new Notification(user.get(), notificationContent, timeSent, type);
        notificationsMap.get(user.get()).add(notification);
        csvProcessor.writeToFile(notification);
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
        notificationsMap.remove(user.get());
        csvProcessor.remove(notification -> notification.receiver().username().equals(username));
    }
}
