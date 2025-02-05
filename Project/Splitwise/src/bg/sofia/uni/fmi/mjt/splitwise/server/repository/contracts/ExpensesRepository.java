package bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts;

import bg.sofia.uni.fmi.mjt.splitwise.server.models.Expense;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;

public interface ExpensesRepository {
    Set<Expense> getExpensesOf(String username);

    void addPersonalBaseExpense(String payerUsername, String participantUsername, double amount, String reason);

    void addGroupExpense(String payerUsername, String groupName, double amount, String reason);

    void exportRecent(String username, int count, Writer writer) throws IOException;
}
