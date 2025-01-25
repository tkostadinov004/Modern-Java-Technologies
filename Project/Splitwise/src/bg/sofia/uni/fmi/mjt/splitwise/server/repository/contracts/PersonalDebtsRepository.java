package bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts;

import bg.sofia.uni.fmi.mjt.splitwise.server.models.PersonalDebt;

import java.util.Optional;
import java.util.Set;

public interface PersonalDebtsRepository {
    Set<PersonalDebt> getDebtsOf(String username);

    Optional<PersonalDebt> getDebtOfDebtorAndRecipient(String debtorUsername, String recipientUsername, String reason);

    void addDebt(String debtorUsername, String recipientUsername, double amount, String reason);

    void updateDebt(String debtorUsername, String recipientUsername, double amount, String reason);
}
