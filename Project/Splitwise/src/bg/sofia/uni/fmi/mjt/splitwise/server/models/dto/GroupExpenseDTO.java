package bg.sofia.uni.fmi.mjt.splitwise.server.models.dto;

import java.time.LocalDateTime;

public record GroupExpenseDTO(String payerUsername,
                             double amount,
                             String reason,
                             String groupName,
                             LocalDateTime timestamp) {
}
