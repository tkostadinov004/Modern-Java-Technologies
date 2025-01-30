package bg.sofia.uni.fmi.mjt.splitwise.server.models.dto;

import bg.sofia.uni.fmi.mjt.splitwise.server.models.FriendGroup;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;

public record GroupDebtDTO(User debtor, User recipient, FriendGroup group, double amount, String reason) {
}
