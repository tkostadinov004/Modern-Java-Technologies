package bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts;

import bg.sofia.uni.fmi.mjt.splitwise.server.models.PersonalExpense;

import java.io.BufferedWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Set;

public interface PersonalExpensesRepository {
    Set<PersonalExpense> getExpensesOf(String username);

    void addExpense(String payerUsername,
                    String participantUsername,
                    double amount, String reason, LocalDateTime timestamp);

    void exportRecent(String username, int count, BufferedWriter writer) throws IOException;
}
