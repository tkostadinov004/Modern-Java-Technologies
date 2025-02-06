package bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts;

import bg.sofia.uni.fmi.mjt.splitwise.server.models.GroupExpense;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.PersonalExpense;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

public interface GroupExpensesRepository {
    Set<GroupExpense> getExpensesOf(String username);

    void addExpense(String payerUsername, String groupName, double amount, String reason);

    void exportRecent(String username, int count, FileWriter writer) throws IOException;
}
