package bg.sofia.uni.fmi.mjt.splitwise.server.models;

import java.time.LocalDateTime;

public record Notification(String content, User receiver, LocalDateTime timeSent) {
}
