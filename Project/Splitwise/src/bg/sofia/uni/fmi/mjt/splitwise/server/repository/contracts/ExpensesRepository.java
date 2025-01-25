package bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts;

import bg.sofia.uni.fmi.mjt.splitwise.server.models.Expense;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;

import java.util.Set;

public interface ExpensesRepository {
    Set<Expense> getExpensesOf(String username);

    void addExpense(String payerUsername, double amount, String purpose, Set<String> participantsUsernames);
}
