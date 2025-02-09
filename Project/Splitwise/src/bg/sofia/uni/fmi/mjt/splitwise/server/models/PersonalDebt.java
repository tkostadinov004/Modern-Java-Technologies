package bg.sofia.uni.fmi.mjt.splitwise.server.models;

public record PersonalDebt(User debtor, User recipient, double amount, String reason) {

}
