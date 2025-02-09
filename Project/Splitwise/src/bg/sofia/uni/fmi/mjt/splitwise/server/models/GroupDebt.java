package bg.sofia.uni.fmi.mjt.splitwise.server.models;

public record GroupDebt(User debtor, User recipient, FriendGroup group, double amount, String reason) {

}

