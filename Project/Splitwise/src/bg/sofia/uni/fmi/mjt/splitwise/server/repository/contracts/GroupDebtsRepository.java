package bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts;

import bg.sofia.uni.fmi.mjt.splitwise.server.models.GroupDebt;

import java.util.Optional;
import java.util.Set;

public interface GroupDebtsRepository {
    Set<GroupDebt> getDebtsOf(String username, String groupName);

    Optional<GroupDebt> getDebtOfDebtorAndRecipient(String debtorUsername,
                                                    String recipientUsername,
                                                    String groupName,
                                                    String reason);

    void addDebt(String debtorUsername,
                 String recipientUsername, String groupName,
                 double amount, String reason);

    void updateDebt(String debtorUsername,
                    String recipientUsername, String groupName,
                    double amount, String reason);
}
