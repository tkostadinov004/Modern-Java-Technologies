package bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.data.CsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.data.implementations.PersonalExpensesCsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.dependency.DependencyContainer;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.NotificationType;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.PersonalExpense;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.dto.PersonalExpenseDTO;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.NotificationsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.PersonalDebtsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.PersonalExpensesRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.NonExistentUserException;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations.converter.DataConverter;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations.converter.PersonalExpensesConverter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DefaultPersonalExpensesRepository implements PersonalExpensesRepository {
    private final Logger logger;
    private final CsvProcessor<PersonalExpenseDTO> csvProcessor;
    private final UserRepository userRepository;
    private final PersonalDebtsRepository personalDebtsRepository;
    private final NotificationsRepository notificationsRepository;
    private final Map<User, Set<PersonalExpense>> expensesMap;

    public DefaultPersonalExpensesRepository(DependencyContainer dependencyContainer) {
        this.logger = dependencyContainer.get(Logger.class);
        this.csvProcessor = dependencyContainer.get(PersonalExpensesCsvProcessor.class);
        this.userRepository = dependencyContainer.get(UserRepository.class);
        this.personalDebtsRepository = dependencyContainer.get(PersonalDebtsRepository.class);
        this.notificationsRepository = dependencyContainer.get(NotificationsRepository.class);

        DataConverter<Map<User, Set<PersonalExpense>>, PersonalExpense, PersonalExpenseDTO> converter =
                new PersonalExpensesConverter(csvProcessor, userRepository);
        this.expensesMap = new ConcurrentHashMap<>(converter.populate(Collectors.groupingBy(PersonalExpense::payer,
                Collectors.mapping(expense -> expense,
                        Collectors.toCollection(() -> Collections.synchronizedSet(new HashSet<>()))))));
    }

    @Override
    public Set<PersonalExpense> getExpensesOf(String username) {
        if (username == null || username.isEmpty() || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null, blank or empty!");
        }

        Optional<User> user = userRepository.getUserByUsername(username);
        if (user.isEmpty()) {
            throw new NonExistentUserException("User with username %s does not exist!".formatted(username));
        }

        synchronized (expensesMap) {
            if (!expensesMap.containsKey(user.get())) {
                return Set.of();
            }

            return new HashSet<>(expensesMap.get(user.get()));
        }
    }

    private void validateArguments(String debtorUsername,
                                   String participantUsername,
                                   double amount,
                                   String reason,
                                   LocalDateTime timestamp) {
        if (debtorUsername == null || debtorUsername.isEmpty() || debtorUsername.isBlank()) {
            throw new IllegalArgumentException("Debtor username cannot be null, blank or empty!");
        }
        if (participantUsername == null || participantUsername.isEmpty() || participantUsername.isBlank()) {
            throw new IllegalArgumentException("Participant username cannot be null, blank or empty!");
        }
        if (reason == null || reason.isEmpty() || reason.isBlank()) {
            throw new IllegalArgumentException("Reason cannot be null, blank or empty!");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Debt amount cannot be less than or equal to 0!");
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("Timestamp cannot be null!");
        }
    }

    @Override
    public void addExpense(String payerUsername,
                           String participantUsername,
                           double amount,
                           String reason,
                           LocalDateTime timestamp) {
        validateArguments(payerUsername, participantUsername, amount, reason, timestamp);

        Optional<User> payer = userRepository.getUserByUsername(payerUsername);
        if (payer.isEmpty()) {
            throw new NonExistentUserException("User with username %s does not exist!".formatted(payerUsername));
        }
        Optional<User> participant = userRepository.getUserByUsername(participantUsername);
        if (participant.isEmpty()) {
            throw new NonExistentUserException("User with username %s does not exist!".formatted(participantUsername));
        }

        expensesMap.putIfAbsent(payer.get(), Collections.synchronizedSet(new HashSet<>()));
        PersonalExpense expense = new PersonalExpense(payer.get(), participant.get(), amount, reason,
                timestamp);
        expensesMap.get(payer.get()).add(expense);
        personalDebtsRepository.increaseDebtBurden(participantUsername, payerUsername, amount / 2.0, reason);
        notificationsRepository.addNotificationForUser(participantUsername,
                "%s noted that they paid %s LV for %s. You owe them %s LV."
                        .formatted(payerUsername, amount, reason, amount / 2.0),
                timestamp, NotificationType.PERSONAL);
        csvProcessor.writeToFile(new PersonalExpenseDTO(expense.payer().username(),
                        expense.debtor().username(),
                        expense.amount(), expense.reason(), expense.timestamp()));
        logger.info("%s paid %s LV for %s".formatted(payerUsername, amount, reason));
    }

    @Override
    public void exportRecent(String username, int count, BufferedWriter writer) throws IOException {
        synchronized (expensesMap) {
            if (username == null || username.isEmpty() || username.isBlank()) {
                throw new IllegalArgumentException("Username cannot be null, blank or empty!");
            }
            if (count <= 0) {
                throw new IllegalArgumentException("Count cannot be less than or equal to 0!");
            }
            if (writer == null) {
                throw new IllegalArgumentException("Writer cannot be null!");
            }

            List<String> expenses = getExpensesOf(username)
                    .stream()
                    .sorted(Comparator.comparing(PersonalExpense::timestamp).reversed())
                    .limit(count)
                    .map(e -> "%s: %s [%s] with %s"
                            .formatted(e.timestamp(),
                                    e.amount(),
                                    e.reason(),
                                    e.debtor()))
                    .toList();

            String content = String.join(System.lineSeparator(), expenses);
            writer.write(content);
            writer.write(System.lineSeparator());
            writer.flush();
        }
    }
}
