package bg.sofia.uni.fmi.mjt.splitwise.server.models.dto;

public record GroupDebtDTO(String debtorUsername,
                           String recipientUsername,
                           String groupName,
                           double amount,
                           String reason) {
}
