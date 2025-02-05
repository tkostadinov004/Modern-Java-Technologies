package bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts;

import bg.sofia.uni.fmi.mjt.splitwise.server.models.PersonalDebt;

import java.util.Set;

public interface PersonalDebtsRepository {
    Set<PersonalDebt> getDebtsOf(String username);

    void lowerDebtBurden(String debtorUsername, String recipientUsername, double amount, String reason);

    void increaseDebtBurden(String debtorUsername, String recipientUsername, double amount, String reason);
}
