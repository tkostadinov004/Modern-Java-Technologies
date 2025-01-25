package bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.models.PersonalDebt;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.PersonalDebtsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.NonExistingUserException;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class DefaultPersonalDebtsRepository implements PersonalDebtsRepository {
    private final UserRepository userRepository;
    private final Set<PersonalDebt> personalDebts;

    public DefaultPersonalDebtsRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.personalDebts = new HashSet<>();
    }

    @Override
    public Set<PersonalDebt> getDebtsOf(String username) {
        if (username == null || username.isEmpty() || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null, blank or empty!");
        }

        Optional<User> user = userRepository.getUserByUsername(username);
        if (user.isEmpty()) {
            throw new NonExistingUserException("User with username %s does not exist!".formatted(username));
        }

        return personalDebts
                .stream()
                .filter(debt -> debt.debtor().equals(user.get()) || debt.recipient().equals(user.get()))
                .collect(Collectors.toCollection(HashSet::new));
    }

    private Optional<PersonalDebt> getDebtWithPayerAndReceiver(User debtor, User recipient, String reason) {
        return personalDebts
                .stream()
                .filter(debt ->
                        ((debt.debtor().equals(debtor) && debt.recipient().equals(recipient)) ||
                        (debt.recipient().equals(debtor) && debt.debtor().equals(recipient))) &&
                        reason.equals(debt.reason()))
                .findFirst();
    }

    @Override
    public Optional<PersonalDebt> getDebtOfDebtorAndRecipient(String debtorUsername, String recipientUsername, String reason) {
        if (debtorUsername == null || debtorUsername.isEmpty() || debtorUsername.isBlank()) {
            throw new IllegalArgumentException("Debtor username cannot be null, blank or empty!");
        }
        if (recipientUsername == null || recipientUsername.isEmpty() || recipientUsername.isBlank()) {
            throw new IllegalArgumentException("Recipient username cannot be null, blank or empty!");
        }
        if (reason == null || reason.isEmpty() || reason.isBlank()) {
            throw new IllegalArgumentException("Reason cannot be null, blank or empty!");
        }

        Optional<User> debtor = userRepository.getUserByUsername(debtorUsername);
        if (debtor.isEmpty()) {
            throw new NonExistingUserException("User with username %s does not exist!".formatted(debtorUsername));
        }
        Optional<User> recipient = userRepository.getUserByUsername(recipientUsername);
        if (recipient.isEmpty()) {
            throw new NonExistingUserException("User with username %s does not exist!".formatted(recipientUsername));
        }

        return getDebtWithPayerAndReceiver(debtor.get(), recipient.get(), reason);
    }

    private void addDebt(User debtor, User recipient, double amount, String reason) {
        PersonalDebt personalDebt = new PersonalDebt(debtor, recipient, amount, reason);
        personalDebts.add(personalDebt);
    }

    @Override
    public void addDebt(String debtorUsername, String recipientUsername, double amount, String reason) {
        if (debtorUsername == null || debtorUsername.isEmpty() || debtorUsername.isBlank()) {
            throw new IllegalArgumentException("Debtor username cannot be null, blank or empty!");
        }
        if (recipientUsername == null || recipientUsername.isEmpty() || recipientUsername.isBlank()) {
            throw new IllegalArgumentException("Recipient username cannot be null, blank or empty!");
        }
        if (reason == null || reason.isEmpty() || reason.isBlank()) {
            throw new IllegalArgumentException("Reason cannot be null, blank or empty!");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Debt amount cannot be less than or equal to 0!");
        }

        Optional<User> debtor = userRepository.getUserByUsername(debtorUsername);
        if (debtor.isEmpty()) {
            throw new NonExistingUserException("User with username %s does not exist!".formatted(debtorUsername));
        }
        Optional<User> recipient = userRepository.getUserByUsername(recipientUsername);
        if (recipient.isEmpty()) {
            throw new NonExistingUserException("User with username %s does not exist!".formatted(recipientUsername));
        }

        addDebt(debtor.get(), recipient.get(), amount, reason);
    }

    private void updateDebt(User debtor, User recipient, double amount, String reason) {
        Optional<PersonalDebt> debt = getDebtWithPayerAndReceiver(debtor, recipient, reason);
        if (debt.isEmpty()) {
            addDebt(debtor, recipient, amount, reason);
            return;
        }

        double newAmount = debt.get().amount() +
                (debt.get().debtor().equals(debtor) ? amount : -amount);
        if (newAmount < 0) {
            debt.get().swapSides();
            newAmount *= (-1);
        }
        debt.get().updateAmount(newAmount);
    }

    @Override
    public void updateDebt(String debtorUsername, String recipientUsername, double amount, String reason) {
        if (debtorUsername == null || debtorUsername.isEmpty() || debtorUsername.isBlank()) {
            throw new IllegalArgumentException("Debtor username cannot be null, blank or empty!");
        }
        if (recipientUsername == null || recipientUsername.isEmpty() || recipientUsername.isBlank()) {
            throw new IllegalArgumentException("Recipient username cannot be null, blank or empty!");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Debt amount cannot be less than or equal to 0!");
        }

        Optional<User> debtor = userRepository.getUserByUsername(debtorUsername);
        if (debtor.isEmpty()) {
            throw new NonExistingUserException("User with username %s does not exist!".formatted(debtorUsername));
        }
        Optional<User> recipient = userRepository.getUserByUsername(recipientUsername);
        if (recipient.isEmpty()) {
            throw new NonExistingUserException("User with username %s does not exist!".formatted(recipientUsername));
        }

        updateDebt(debtor.get(), recipient.get(), amount, reason);
    }
}
