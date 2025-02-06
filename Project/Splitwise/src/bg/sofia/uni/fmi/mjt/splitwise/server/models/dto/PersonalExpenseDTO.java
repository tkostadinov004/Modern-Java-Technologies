package bg.sofia.uni.fmi.mjt.splitwise.server.models.dto;

import java.time.LocalDateTime;

public record PersonalExpenseDTO(String payerUsername,
                                 String debtorUsername,
                                 double amount,
                                 String reason,
                                 LocalDateTime timestamp) {
}
