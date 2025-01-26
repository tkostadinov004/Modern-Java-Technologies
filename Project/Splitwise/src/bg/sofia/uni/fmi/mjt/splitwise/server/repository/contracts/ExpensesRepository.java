package bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts;

import bg.sofia.uni.fmi.mjt.splitwise.server.models.Expense;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.FriendGroup;

import java.util.Set;

public interface ExpensesRepository {
    Set<Expense> getExpensesOf(String username);

    void addPersonalBaseExpense(String payerUsername, String participantUsername, double amount, String purpose);

    void addGroupExpense(String payerUsername, String groupName, double amount, String purpose);
}
