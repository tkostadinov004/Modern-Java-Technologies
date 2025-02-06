package bg.sofia.uni.fmi.mjt.splitwise.server.models;

import java.time.LocalDateTime;

public record PersonalExpense(User payer,
                              User debtor,
                              double amount,
                              String reason,
                              LocalDateTime timestamp) {
}
