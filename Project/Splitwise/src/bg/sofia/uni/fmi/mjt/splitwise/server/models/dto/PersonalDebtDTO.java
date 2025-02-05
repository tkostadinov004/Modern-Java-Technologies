package bg.sofia.uni.fmi.mjt.splitwise.server.models.dto;

public record PersonalDebtDTO(String debtorUsername, String recipientUsername, double amount, String reason) {
}
