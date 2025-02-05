package bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts;

import bg.sofia.uni.fmi.mjt.splitwise.server.models.Notification;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.NotificationType;

import java.time.LocalDateTime;
import java.util.Set;

public interface NotificationsRepository {
    Set<Notification> getNotificationForUser(String username);

    void addNotificationForUser(String username,
                                String notificationContent,
                                LocalDateTime timeSent,
                                NotificationType type);

    void removeAllNotificationsForUser(String username);
}
