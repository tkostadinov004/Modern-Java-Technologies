package bg.sofia.uni.fmi.mjt.splitwise.server.models;

import java.time.LocalDateTime;
import java.util.Set;

public record Expense(User payer, double amount, String purpose, LocalDateTime timestamp, Set<User> participants) {
}
