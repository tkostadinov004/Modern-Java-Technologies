package bg.sofia.uni.fmi.mjt.splitwise.server.models.dto;

import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;

public record PersonalDebtDTO(User debtor, User recipient, double amount, String reason) {
}
