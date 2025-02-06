package bg.sofia.uni.fmi.mjt.splitwise.server.models;

import java.time.LocalDateTime;

public record GroupExpense(User payer,
                           double amount,
                           String reason,
                           FriendGroup group,
                           LocalDateTime timestamp) {
}
