package bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts;

import bg.sofia.uni.fmi.mjt.splitwise.server.models.FriendGroup;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.GroupDebt;

import java.util.Map;
import java.util.Set;

public interface GroupDebtsRepository {
    Map<FriendGroup, Set<GroupDebt>> getDebtsOf(String username);

    void lowerDebtBurden(String debtorUsername,
                         String recipientUsername,
                         String groupName,
                         double amount, String reason);

    void increaseDebtBurden(String debtorUsername,
                            String recipientUsername,
                            String groupName,
                            double amount, String reason);
}
