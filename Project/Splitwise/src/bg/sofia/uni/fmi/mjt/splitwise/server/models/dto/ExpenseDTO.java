package bg.sofia.uni.fmi.mjt.splitwise.server.models.dto;

import java.time.LocalDateTime;
import java.util.Set;

public record ExpenseDTO(String payerUsername,
                         double amount,
                         String reason,
                         LocalDateTime timestamp,
                         Set<String> participantsUsernames) {
}
