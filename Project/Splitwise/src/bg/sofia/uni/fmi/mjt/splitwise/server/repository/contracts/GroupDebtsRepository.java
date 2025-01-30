package bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts;

import bg.sofia.uni.fmi.mjt.splitwise.server.models.FriendGroup;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.Debt;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface GroupDebtsRepository {
    Map<FriendGroup, Set<Debt>> getDebtsOf(String username);

    Optional<Debt> getDebtOfDebtorAndRecipient(String debtorUsername,
                                               String recipientUsername,
                                               String groupName,
                                               String reason);

    void addDebt(String debtorUsername,
                 String recipientUsername, String groupName,
                 double amount, String reason);

    void lowerDebtBurden(String debtorUsername,
                         String recipientUsername, String groupName,
                         double amount, String reason);

    void increaseDebtBurden(String debtorUsername,
                         String recipientUsername, String groupName,
                         double amount, String reason);
}
