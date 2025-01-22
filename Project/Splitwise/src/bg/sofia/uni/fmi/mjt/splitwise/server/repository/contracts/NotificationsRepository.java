package bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts;

import bg.sofia.uni.fmi.mjt.splitwise.server.models.Notification;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;

import java.time.LocalDateTime;
import java.util.Set;

public interface NotificationsRepository {
    Set<Notification> getNotificationForUser(String username);

    Set<Notification> getNotificationForUser(User user);

    void addNotificationForUser(String username, String notificationContent, LocalDateTime timeSent);

    void addNotificationForUser(User user, String notificationContent, LocalDateTime timeSent);

    void removeAllNotificationsForUser(String username);

    void removeAllNotificationsForUser(User user);
}
