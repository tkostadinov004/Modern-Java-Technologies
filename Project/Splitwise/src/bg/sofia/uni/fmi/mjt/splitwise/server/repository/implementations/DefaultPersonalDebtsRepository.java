package bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.data.CsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.data.implementations.PersonalDebtsCsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.dependency.DependencyContainer;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.PersonalDebt;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.NotificationType;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.dto.PersonalDebtDTO;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.NotificationsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.PersonalDebtsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.NonExistentUserException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class DefaultPersonalDebtsRepository implements PersonalDebtsRepository {
    private final CsvProcessor<PersonalDebtDTO> csvProcessor;
    private final UserRepository userRepository;
    private final NotificationsRepository notificationsRepository;
    private final Set<PersonalDebt> personalDebts;

    private PersonalDebt createFromDTO(PersonalDebtDTO dto) {
        Optional<User> debtor = userRepository.getUserByUsername(dto.debtorUsername());
        if (debtor.isEmpty()) {
            return null;
        }
        Optional<User> recipient = userRepository.getUserByUsername(dto.recipientUsername());
        if (recipient.isEmpty()) {
            return null;
        }
        return new PersonalDebt(debtor.get(), recipient.get(), dto.amount(), dto.reason());
    }

    private Set<PersonalDebt> populateDebts() {
        return csvProcessor
                .readAll()
                .stream()
                .map(this::createFromDTO)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public DefaultPersonalDebtsRepository(DependencyContainer dependencyContainer) {
        this.csvProcessor = dependencyContainer.get(PersonalDebtsCsvProcessor.class);
        this.userRepository = dependencyContainer.get(UserRepository.class);
        this.notificationsRepository = dependencyContainer.get(NotificationsRepository.class);
        this.personalDebts = populateDebts();
    }

    @Override
    public Set<PersonalDebt> getDebtsOf(String username) {
        if (username == null || username.isEmpty() || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null, blank or empty!");
        }

        Optional<User> user = userRepository.getUserByUsername(username);
        if (user.isEmpty()) {
            throw new NonExistentUserException("User with username %s does not exist!".formatted(username));
        }

        return personalDebts
                .stream()
                .filter(debt -> debt.debtor().equals(user.get()) || debt.recipient().equals(user.get()))
                .collect(Collectors.toCollection(HashSet::new));
    }

    private Optional<PersonalDebt> getDebtOfDebtorAndRecipient(User debtor, User recipient, String reason) {
        return personalDebts
                .stream()
                .filter(debt ->
                        ((debt.debtor().equals(debtor) && debt.recipient().equals(recipient)) ||
                        (debt.recipient().equals(debtor) && debt.debtor().equals(recipient))) &&
                        reason.equals(debt.reason()))
                .findFirst();
    }

    private void addDebt(User debtor, User recipient, double amount, String reason) {
        PersonalDebt personalDebt = new PersonalDebt(debtor, recipient, amount, reason);
        personalDebts.add(personalDebt);
        csvProcessor.writeToFile(new PersonalDebtDTO(debtor.username(), recipient.username(), amount, reason));
    }

    private void lowerDebtBurden(PersonalDebt debt, double amount) {
        double newAmount = debt.amount() - amount;

        if (newAmount == 0) {
            personalDebts.remove(debt);

            csvProcessor.remove(d -> d.debtorUsername().equals(debt.debtor().username()) &&
                    d.recipientUsername().equals(debt.recipient().username()) &&
                    d.reason().equals(debt.reason()));
        } else {
            debt.updateAmount(newAmount);

            PersonalDebtDTO updatedDebtDTO = new PersonalDebtDTO(debt.debtor().username(),
                    debt.recipient().username(),
                    newAmount,
                    debt.reason());

            csvProcessor.modify(d -> d.debtorUsername().equals(debt.debtor().username()) &&
                            d.recipientUsername().equals(debt.recipient().username()) &&
                            d.reason().equals(debt.reason()), updatedDebtDTO);
        }
        notificationsRepository.addNotificationForUser(debt.debtor().username(),
                "%s approved your payment of %s LV for %s. You now owe them %s LV."
                        .formatted(debt.recipient().username(), amount, debt.reason(), newAmount),
                LocalDateTime.now(),
                NotificationType.PERSONAL);
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
            throw new NonExistentUserException("User with username %s does not exist!".formatted(debtorUsername));
        }
        Optional<User> recipient = userRepository.getUserByUsername(recipientUsername);
        if (recipient.isEmpty()) {
            throw new NonExistentUserException("User with username %s does not exist!".formatted(recipientUsername));
        }

        Optional<PersonalDebt> debt = getDebtOfDebtorAndRecipient(debtor.get(), recipient.get(), reason);
        if (debt.isEmpty()) {
            addDebt(debtor.get(), recipient.get(), amount, reason);
            return;
        }
        lowerDebtBurden(debt.get(), amount);
    }

    private void increaseDebtBurden(PersonalDebt debt, double amount) {
        double newAmount = debt.amount() + amount;
        debt.updateAmount(newAmount);

        PersonalDebtDTO updatedDebtDTO = new PersonalDebtDTO(debt.debtor().username(),
                debt.recipient().username(),
                newAmount,
                debt.reason());

        csvProcessor.modify(d -> d.debtorUsername().equals(debt.debtor().username()) &&
                        d.recipientUsername().equals(debt.recipient().username()) &&
                        d.reason().equals(debt.reason()), updatedDebtDTO);
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
            throw new NonExistentUserException("User with username %s does not exist!".formatted(debtorUsername));
        }
        Optional<User> recipient = userRepository.getUserByUsername(recipientUsername);
        if (recipient.isEmpty()) {
            throw new NonExistentUserException("User with username %s does not exist!".formatted(recipientUsername));
        }
        Optional<PersonalDebt> debt = getDebtOfDebtorAndRecipient(debtor.get(), recipient.get(), reason);
        if (debt.isEmpty()) {
            addDebt(debtor.get(), recipient.get(), amount, reason);
            return;
        }

        increaseDebtBurden(debt.get(), amount);
    }
}
