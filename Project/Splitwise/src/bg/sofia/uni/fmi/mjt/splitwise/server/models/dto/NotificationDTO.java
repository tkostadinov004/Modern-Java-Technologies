package bg.sofia.uni.fmi.mjt.splitwise.server.models.dto;

import bg.sofia.uni.fmi.mjt.splitwise.server.models.NotificationType;

import java.time.LocalDateTime;

public record NotificationDTO(String receiverUsername, String content, LocalDateTime timeSent, NotificationType type) {
}
