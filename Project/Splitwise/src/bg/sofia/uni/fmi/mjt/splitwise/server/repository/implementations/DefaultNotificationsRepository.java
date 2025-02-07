package bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.data.CsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.data.implementations.NotificationsCsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.dependency.DependencyContainer;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.Notification;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.NotificationType;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.dto.NotificationDTO;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.NotificationsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.NonExistentUserException;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations.converter.DataConverter;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations.converter.NotificationsConverter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DefaultNotificationsRepository implements NotificationsRepository {
    private final CsvProcessor<NotificationDTO> csvProcessor;
    private final UserRepository userRepository;
    private final Map<User, Set<Notification>> notificationsMap;

    public DefaultNotificationsRepository(DependencyContainer dependencyContainer) {
        this.csvProcessor = dependencyContainer.get(NotificationsCsvProcessor.class);
        this.userRepository = dependencyContainer.get(UserRepository.class);

        DataConverter<Map<User, Set<Notification>>, Notification, NotificationDTO> converter =
                new NotificationsConverter(csvProcessor, userRepository);
        this.notificationsMap = new ConcurrentHashMap<>(converter.populate(Collectors.groupingBy(Notification::receiver,
                Collectors.mapping(rel -> rel,
                        Collectors.toCollection(() -> Collections.synchronizedSet(new HashSet<>()))))));
    }

    @Override
    public Set<Notification> getNotificationsForUser(String username) {
        if (username == null || username.isEmpty() || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null, blank or empty!");
        }

        Optional<User> user = userRepository.getUserByUsername(username);
        if (user.isEmpty()) {
            throw new NonExistentUserException("User with username %s does not exist!".formatted(username));
        }

        synchronized (notificationsMap) {
            if (!notificationsMap.containsKey(user.get())) {
                return Set.of();
            }
            return new HashSet<>(notificationsMap.get(user.get()));
        }
    }

    @Override
    public void addNotificationForUser(String username,
                                       String notificationContent,
                                       LocalDateTime timeSent,
                                       NotificationType type) {
        if (username == null || username.isEmpty() || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null, blank or empty!");
        }
        if (notificationContent == null || notificationContent.isEmpty() || notificationContent.isBlank()) {
            throw new IllegalArgumentException("Notification content cannot be null, blank or empty!");
        }
        if (timeSent == null) {
            throw new IllegalArgumentException("Time sent cannot be null!");
        }
        if (type == null) {
            throw new IllegalArgumentException("Notification type cannot be null!");
        }

        Optional<User> user = userRepository.getUserByUsername(username);
        if (user.isEmpty()) {
            throw new NonExistentUserException("User with username %s does not exist!".formatted(username));
        }

        notificationsMap.putIfAbsent(user.get(), Collections.synchronizedSet(new HashSet<>()));

        Notification notification = new Notification(user.get(), notificationContent, timeSent, type);
        notificationsMap.get(user.get()).add(notification);
        csvProcessor.writeToFile(new NotificationDTO(username, notificationContent, timeSent, type));
    }

    @Override
    public void addNotificationForUser(String username, String notificationContent, NotificationType type) {
        addNotificationForUser(username, notificationContent, LocalDateTime.now(), type);
    }

    @Override
    public void removeAllNotificationsForUser(String username) {
        if (username == null || username.isEmpty() || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null, blank or empty!");
        }

        Optional<User> user = userRepository.getUserByUsername(username);
        if (user.isEmpty()) {
            throw new NonExistentUserException("User with username %s does not exist!".formatted(username));
        }
        notificationsMap.remove(user.get());
        csvProcessor.removeAll(notification -> notification.receiverUsername().equals(username));
    }
}
