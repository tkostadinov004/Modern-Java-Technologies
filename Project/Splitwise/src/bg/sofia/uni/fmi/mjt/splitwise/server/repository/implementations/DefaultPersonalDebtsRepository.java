package bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.data.CsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.Debt;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.NotificationType;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.dto.GroupDebtDTO;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.dto.PersonalDebtDTO;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.NotificationsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.PersonalDebtsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.NonExistingUserException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class DefaultPersonalDebtsRepository implements PersonalDebtsRepository {
    private final CsvProcessor<PersonalDebtDTO> csvProcessor;
    private final UserRepository userRepository;
    private final NotificationsRepository notificationsRepository;
    private final Set<Debt> personalDebts;

    private Set<Debt> populateDebts() {
        return csvProcessor
                .readAll()
                .stream()
                .map(dto -> new Debt(dto.debtor(), dto.recipient(), dto.amount(), dto.reason()))
                .collect(Collectors.toSet());
    }

    public DefaultPersonalDebtsRepository(CsvProcessor<PersonalDebtDTO> csvProcessor, UserRepository userRepository, NotificationsRepository notificationsRepository) {
        this.csvProcessor = csvProcessor;
        this.userRepository = userRepository;
        this.notificationsRepository = notificationsRepository;
        this.personalDebts = populateDebts();
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

        return personalDebts
                .stream()
                .filter(debt -> debt.debtor().equals(user.get()) || debt.recipient().equals(user.get()))
                .collect(Collectors.toCollection(HashSet::new));
    }

    private Optional<Debt> getDebtOfDebtorAndRecipient(User debtor, User recipient, String reason) {
        return personalDebts
                .stream()
                .filter(debt ->
                        ((debt.debtor().equals(debtor) && debt.recipient().equals(recipient)) ||
                        (debt.recipient().equals(debtor) && debt.debtor().equals(recipient))) &&
                        reason.equals(debt.reason()))
                .findFirst();
    }

    @Override
    public Optional<Debt> getDebtOfDebtorAndRecipient(String debtorUsername, String recipientUsername, String reason) {
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

        return getDebtOfDebtorAndRecipient(debtor.get(), recipient.get(), reason);
    }

    private void addDebt(User debtor, User recipient, double amount, String reason) {
        Debt personalDebt = new Debt(debtor, recipient, amount, reason);
        personalDebts.add(personalDebt);
        csvProcessor.writeToFile(new PersonalDebtDTO(debtor, recipient, amount, reason));
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

    @Override
    public void lowerDebtBurden(String debtorUsername, String recipientUsername, double amount, String reason) {
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

        Optional<Debt> debt = getDebtOfDebtorAndRecipient(debtor.get(), recipient.get(), reason);
        if (debt.isEmpty()) {
            addDebt(debtor.get(), recipient.get(), amount, reason);
            return;
        }

        double newAmount = debt.get().amount() - amount;
        if (newAmount == 0) {
            personalDebts.remove(debt.get());
            csvProcessor.remove(d -> d.debtor().username().equals(debtorUsername) && d.recipient().username().equals(recipientUsername) && d.reason().equals(reason));
        } else {
            debt.get().updateAmount(newAmount);
            csvProcessor.modify(d -> d.debtor().username().equals(debtorUsername) && d.recipient().username().equals(recipientUsername) && d.reason().equals(reason),
                    new PersonalDebtDTO(debt.get().debtor(), debt.get().recipient(), newAmount, debt.get().reason()));
        }
        notificationsRepository.addNotificationForUser(debtorUsername,
                "%s approved your payment of %s LV for %s. You now owe them %s LV.".formatted(recipientUsername, amount, reason, newAmount),
                LocalDateTime.now(),
                NotificationType.PERSONAL);
    }

    @Override
    public void increaseDebtBurden(String debtorUsername, String recipientUsername, double amount, String reason) {
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

        Optional<Debt> debt = getDebtOfDebtorAndRecipient(debtor.get(), recipient.get(), reason);
        if (debt.isEmpty()) {
            addDebt(debtor.get(), recipient.get(), amount, reason);
            return;
        }

        double newAmount = debt.get().amount() + amount;
        debt.get().updateAmount(newAmount);
        csvProcessor.modify(d -> d.debtor().username().equals(debtorUsername) && d.recipient().username().equals(recipientUsername) && d.reason().equals(reason),
                new PersonalDebtDTO(debt.get().debtor(), debt.get().recipient(), newAmount, debt.get().reason()));
    }
}
