package bg.sofia.uni.fmi.mjt.splitwise.server.models;

public record Debt(User payer, User receiver, double amount) {
}
