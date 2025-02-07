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
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.NonExistentDebtException;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.NonExistentUserException;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations.converter.DataConverter;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations.converter.PersonalDebtsConverter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class DefaultPersonalDebtsRepository implements PersonalDebtsRepository {
    private final CsvProcessor<PersonalDebtDTO> csvProcessor;
    private final UserRepository userRepository;
    private final NotificationsRepository notificationsRepository;
    private final Set<PersonalDebt> personalDebts;

    public DefaultPersonalDebtsRepository(DependencyContainer dependencyContainer) {
        this.csvProcessor = dependencyContainer.get(PersonalDebtsCsvProcessor.class);
        this.userRepository = dependencyContainer.get(UserRepository.class);
        this.notificationsRepository = dependencyContainer.get(NotificationsRepository.class);

        DataConverter<Set<PersonalDebt>, PersonalDebt, PersonalDebtDTO> converter =
                new PersonalDebtsConverter(csvProcessor, userRepository);
        this.personalDebts = Collections.synchronizedSet(new HashSet<>(converter.populate(Collectors.toSet())));
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

        synchronized (personalDebts) {
            return personalDebts
                    .stream()
                    .filter(debt -> debt.debtor().equals(user.get()) || debt.recipient().equals(user.get()))
                    .collect(Collectors.toCollection(HashSet::new));
        }
    }

    private Optional<PersonalDebt> getDebtOfDebtorAndRecipient(User debtor, User recipient, String reason) {
        synchronized (personalDebts) {
            return personalDebts
                       .stream()
                       .filter(debt ->
                               ((debt.debtor().equals(debtor) && debt.recipient().equals(recipient)) ||
                                       (debt.recipient().equals(debtor) && debt.debtor().equals(recipient))) &&
                                       reason.equals(debt.reason()))
                       .findFirst();
        }
    }

    private void addDebt(User debtor, User recipient, double amount, String reason) {
        PersonalDebt personalDebt = new PersonalDebt(debtor, recipient, amount, reason);
        personalDebts.add(personalDebt);
        csvProcessor.writeToFile(new PersonalDebtDTO(debtor.username(), recipient.username(), amount, reason));
    }

    private void lowerDebtBurden(PersonalDebt debt, double amount, boolean isReversed) {
        synchronized (personalDebts) {
            double newAmount = debt.amount() - amount;

            PersonalDebtDTO crudDTO = new PersonalDebtDTO(debt.debtor().username(), debt.recipient().username(),
                    debt.amount(), debt.reason());

            if (newAmount <= 0) {
                personalDebts.remove(debt);
                csvProcessor.remove(crudDTO);
                if (isReversed && newAmount < 0) {
                    addDebt(debt.recipient(), debt.debtor(), Math.abs(newAmount), debt.reason());
                }
            } else {
                debt.updateAmount(newAmount);

                PersonalDebtDTO updatedDebtDTO = new PersonalDebtDTO(debt.debtor().username(),
                        debt.recipient().username(),
                        newAmount,
                        debt.reason());

                csvProcessor.modify(crudDTO, updatedDebtDTO);
            }
            notificationsRepository.addNotificationForUser(debt.debtor().username(),
                    "%s approved your payment of %s LV for %s. You now owe them %s LV."
                            .formatted(debt.recipient().username(), amount, debt.reason(), newAmount),
                    NotificationType.PERSONAL);
        }
    }

    private void validateArguments(String debtorUsername, String recipientUsername, double amount, String reason) {
        if (debtorUsername == null || debtorUsername.isEmpty() || debtorUsername.isBlank()) {
            throw new IllegalArgumentException("Debtor username cannot be null, blank or empty!");
        }
        if (recipientUsername == null || recipientUsername.isEmpty() || recipientUsername.isBlank()) {
            throw new IllegalArgumentException("Recipient username cannot be null, blank or empty!");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Debt amount cannot be less than or equal to 0!");
        }
        if (reason == null || reason.isEmpty() || reason.isBlank()) {
            throw new IllegalArgumentException("Reason cannot be null, blank or empty!");
        }
        if (debtorUsername.equals(recipientUsername)) {
            throw new IllegalArgumentException("Debtor and recipient cannot be the same person!");
        }
    }

    @Override
    public void lowerDebtBurden(String debtorUsername, String recipientUsername, double amount, String reason) {
        validateArguments(debtorUsername, recipientUsername, amount, reason);

        Optional<User> debtor = userRepository.getUserByUsername(debtorUsername);
        if (debtor.isEmpty()) {
            throw new NonExistentUserException("User with username %s does not exist!".formatted(debtorUsername));
        }
        Optional<User> recipient = userRepository.getUserByUsername(recipientUsername);
        if (recipient.isEmpty()) {
            throw new NonExistentUserException("User with username %s does not exist!".formatted(recipientUsername));
        }

        synchronized (personalDebts) {
            Optional<PersonalDebt> debt = getDebtOfDebtorAndRecipient(debtor.get(), recipient.get(), reason);
            if (debt.isEmpty()) {
                throw new NonExistentDebtException("Debt with debtor %s and reason \"%s\" doesn't exist!"
                        .formatted(debtorUsername, reason));
            }
            lowerDebtBurden(debt.get(), amount, false);
        }
    }

    private void increaseDebtBurden(PersonalDebt debt, double amount) {
        synchronized (personalDebts) {
            double newAmount = debt.amount() + amount;
            PersonalDebtDTO crudDTO = new PersonalDebtDTO(debt.debtor().username(), debt.recipient().username(),
                    debt.amount(), debt.reason());

            debt.updateAmount(newAmount);

            PersonalDebtDTO updatedDebtDTO = new PersonalDebtDTO(debt.debtor().username(),
                    debt.recipient().username(),
                    newAmount,
                    debt.reason());

            csvProcessor.modify(crudDTO, updatedDebtDTO);
        }
    }

    @Override
    public void increaseDebtBurden(String debtorUsername, String recipientUsername, double amount, String reason) {
        validateArguments(debtorUsername, recipientUsername, amount, reason);

        Optional<User> debtor = userRepository.getUserByUsername(debtorUsername);
        if (debtor.isEmpty()) {
            throw new NonExistentUserException("User with username %s does not exist!".formatted(debtorUsername));
        }
        Optional<User> recipient = userRepository.getUserByUsername(recipientUsername);
        if (recipient.isEmpty()) {
            throw new NonExistentUserException("User with username %s does not exist!".formatted(recipientUsername));
        }

        synchronized (personalDebts) {
            Optional<PersonalDebt> debt = getDebtOfDebtorAndRecipient(debtor.get(), recipient.get(), reason);
            if (debt.isEmpty()) {
                addDebt(debtor.get(), recipient.get(), amount, reason);
            } else if (debt.get().debtor().equals(recipient.get())) {
                lowerDebtBurden(debt.get(), amount, true);
            } else {
                increaseDebtBurden(debt.get(), amount);
            }
        }
    }
}