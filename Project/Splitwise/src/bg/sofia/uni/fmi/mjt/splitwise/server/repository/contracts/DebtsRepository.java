package bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts;

import bg.sofia.uni.fmi.mjt.splitwise.server.models.Debt;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;

import java.util.Set;

public interface DebtsRepository {
    Set<Debt> getDebtsOf(String username);

    Set<Debt> getDebtsOf(User user);

    Debt getDebtWithPayerAndReceiver(String payerUsername, String receiverUsername);

    Debt getDebtWithPayerAndReceiver(User payer, User receiver);

    void addDebt(String payerUsername, String receiverUsername, double amount);

    void addDebt(User payer, User receiver, double amount);

    void updateDebt(String payerUsername, String receiverUsername, double amount);

    void updateDebt(User payer, User receiver, double amount);
}
