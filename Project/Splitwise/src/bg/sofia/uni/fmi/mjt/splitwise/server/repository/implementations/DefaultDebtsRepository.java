package bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.models.Debt;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.DebtsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.NonExistingUserException;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class DefaultDebtsRepository implements DebtsRepository {
    private final UserRepository userRepository;
    private final Set<Debt> debts;

    public DefaultDebtsRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.debts = new HashSet<>();
    }

    @Override
    public Set<Debt> getDebtsOf(String username) {
        if (username == null || username.isEmpty() || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null, blank or empty!");
        }

        Optional<User> user = userRepository.getUserByUsername(username);
        if (user.isEmpty()) {
            throw new NonExistingUserException("User with username %s does not exist!".formatted(username));
        }

        return getDebtsOf(user.get());
    }

    @Override
    public Set<Debt> getDebtsOf(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null!");
        }

        return debts
                .stream()
                .filter(debt -> debt.receiver().equals(user))
                .collect(Collectors.toCollection(HashSet::new));
    }

    @Override
    public Optional<Debt> getDebtWithPayerAndReceiver(String payerUsername, String receiverUsername) {
        if (payerUsername == null || payerUsername.isEmpty() || payerUsername.isBlank()) {
            throw new IllegalArgumentException("Payer username cannot be null, blank or empty!");
        }
        if (receiverUsername == null || receiverUsername.isEmpty() || receiverUsername.isBlank()) {
            throw new IllegalArgumentException("Receiver username cannot be null, blank or empty!");
        }

        Optional<User> payer = userRepository.getUserByUsername(payerUsername);
        if (payer.isEmpty()) {
            throw new NonExistingUserException("User with username %s does not exist!".formatted(payerUsername));
        }
        Optional<User> receiver = userRepository.getUserByUsername(receiverUsername);
        if (receiver.isEmpty()) {
            throw new NonExistingUserException("User with username %s does not exist!".formatted(receiverUsername));
        }

        return getDebtWithPayerAndReceiver(payer.get(), receiver.get());
    }

    @Override
    public Optional<Debt> getDebtWithPayerAndReceiver(User payer, User receiver) {
        if (payer == null) {
            throw new IllegalArgumentException("Payer cannot be null!");
        }
        if (receiver == null) {
            throw new IllegalArgumentException("Receiver cannot be null!");
        }

        return debts
                .stream()
                .filter(debt -> (debt.payer().equals(payer) && debt.receiver().equals(receiver)) ||
                        (debt.receiver().equals(payer) && debt.payer().equals(receiver)))
                .findFirst();
    }

    @Override
    public void addDebt(String payerUsername, String receiverUsername, double amount) {
        if (payerUsername == null || payerUsername.isEmpty() || payerUsername.isBlank()) {
            throw new IllegalArgumentException("Payer username cannot be null, blank or empty!");
        }
        if (receiverUsername == null || receiverUsername.isEmpty() || receiverUsername.isBlank()) {
            throw new IllegalArgumentException("Receiver username cannot be null, blank or empty!");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Debt amount cannot be less than or equal to 0!");
        }

        Optional<User> payer = userRepository.getUserByUsername(payerUsername);
        if (payer.isEmpty()) {
            throw new NonExistingUserException("User with username %s does not exist!".formatted(payerUsername));
        }
        Optional<User> receiver = userRepository.getUserByUsername(receiverUsername);
        if (receiver.isEmpty()) {
            throw new NonExistingUserException("User with username %s does not exist!".formatted(receiverUsername));
        }

        addDebt(payer.get(), receiver.get(), amount);
    }

    @Override
    public void addDebt(User payer, User receiver, double amount) {
        if (payer == null) {
            throw new IllegalArgumentException("Payer cannot be null!");
        }
        if (receiver == null) {
            throw new IllegalArgumentException("Receiver cannot be null!");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Debt amount cannot be less than or equal to 0!");
        }

        Debt debt = new Debt(payer, receiver, amount);
        debts.add(debt);
    }

    @Override
    public void updateDebt(String payerUsername, String receiverUsername, double amount) {
        if (payerUsername == null || payerUsername.isEmpty() || payerUsername.isBlank()) {
            throw new IllegalArgumentException("Payer username cannot be null, blank or empty!");
        }
        if (receiverUsername == null || receiverUsername.isEmpty() || receiverUsername.isBlank()) {
            throw new IllegalArgumentException("Receiver username cannot be null, blank or empty!");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Debt amount cannot be less than or equal to 0!");
        }

        Optional<User> payer = userRepository.getUserByUsername(payerUsername);
        if (payer.isEmpty()) {
            throw new NonExistingUserException("User with username %s does not exist!".formatted(payerUsername));
        }
        Optional<User> receiver = userRepository.getUserByUsername(receiverUsername);
        if (receiver.isEmpty()) {
            throw new NonExistingUserException("User with username %s does not exist!".formatted(receiverUsername));
        }

        updateDebt(payer.get(), receiver.get(), amount);
    }

    @Override
    public void updateDebt(User payer, User receiver, double amount) {
        if (payer == null) {
            throw new IllegalArgumentException("Payer cannot be null!");
        }
        if (receiver == null) {
            throw new IllegalArgumentException("Receiver cannot be null!");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Debt amount cannot be less than or equal to 0!");
        }

        Optional<Debt> debt = getDebtWithPayerAndReceiver(payer, receiver);
        if (debt.isEmpty()) {
            addDebt(payer, receiver, amount);
            return;
        }

        double newAmount = debt.get().amount() +
                (debt.get().payer().equals(receiver) ? -amount : amount);
        if (newAmount < 0) {
            debt.get().swapSides();
            newAmount *= (-1);
        }
        debt.get().updateAmount(newAmount);
    }
}
