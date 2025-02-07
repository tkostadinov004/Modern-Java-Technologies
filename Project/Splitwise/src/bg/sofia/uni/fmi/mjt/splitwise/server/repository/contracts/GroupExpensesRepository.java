package bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts;

import bg.sofia.uni.fmi.mjt.splitwise.server.models.GroupExpense;

import java.io.BufferedWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Set;

public interface GroupExpensesRepository {
    Set<GroupExpense> getExpensesOf(String username);

    void addExpense(String payerUsername, String groupName, double amount, String reason, LocalDateTime timestamp);

    void exportRecent(String username, int count, BufferedWriter writer) throws IOException;
}
