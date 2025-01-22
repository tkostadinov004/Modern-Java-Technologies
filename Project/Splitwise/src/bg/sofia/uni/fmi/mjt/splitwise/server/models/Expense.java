package bg.sofia.uni.fmi.mjt.splitwise.server.models;

import java.util.Set;

public record Expense(User payer, double amount, String purpose, Set<User> participants) {
}
