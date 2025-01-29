package bg.sofia.uni.fmi.mjt.splitwise.server.models;

import java.time.LocalDateTime;

public record Notification(String content, LocalDateTime timeSent, NotificationType type) {
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[').append(timeSent).append("]: ");
        sb.append(content);

        return sb.toString().stripTrailing();
    }
}
