package bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.data.CsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.data.implementations.ExpensesCsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.dependency.DependencyContainer;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.Expense;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.FriendGroup;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.NotificationType;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.dto.ExpenseDTO;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.FriendGroupRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.GroupDebtsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.NotificationsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.PersonalDebtsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.ExpensesRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.NonExistentUserException;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations.converter.DataConverter;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations.converter.ExpensesConverter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DefaultExpensesRepository implements ExpensesRepository {
    private final Logger logger;
    private final CsvProcessor<ExpenseDTO> csvProcessor;
    private final UserRepository userRepository;
    private final FriendGroupRepository friendGroupRepository;
    private final PersonalDebtsRepository personalDebtsRepository;
    private final GroupDebtsRepository groupDebtsRepository;
    private final NotificationsRepository notificationsRepository;
    private final Map<User, Set<Expense>> expensesMap;

    public DefaultExpensesRepository(DependencyContainer dependencyContainer) {
        this.logger = dependencyContainer.get(Logger.class);
        this.csvProcessor = dependencyContainer.get(ExpensesCsvProcessor.class);
        this.userRepository = dependencyContainer.get(UserRepository.class);
        this.friendGroupRepository = dependencyContainer.get(FriendGroupRepository.class);
        this.personalDebtsRepository = dependencyContainer.get(PersonalDebtsRepository.class);
        this.groupDebtsRepository = dependencyContainer.get(GroupDebtsRepository.class);
        this.notificationsRepository = dependencyContainer.get(NotificationsRepository.class);

        DataConverter<Map<User, Set<Expense>>, Expense, ExpenseDTO> converter =
                new ExpensesConverter(csvProcessor, userRepository);
        this.expensesMap = converter.populate(Collectors.groupingBy(Expense::payer,
                Collectors.mapping(expense -> expense, Collectors.toSet())));
    }

    private Set<Expense> getExpensesOf(User user) {
        if (!expensesMap.containsKey(user)) {
            return Set.of();
        }

        return expensesMap.get(user);
    }

    @Override
    public Set<Expense> getExpensesOf(String username) {
        if (username == null || username.isEmpty() || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null, blank or empty!");
        }

        Optional<User> user = userRepository.getUserByUsername(username);
        if (user.isEmpty()) {
            throw new NonExistentUserException("User with username %s does not exist!".formatted(username));
        }

        return getExpensesOf(user.get());
    }

    private void validateArguments(String debtorUsername,
                                   double amount,
                                   String reason) {
        if (debtorUsername == null || debtorUsername.isEmpty() || debtorUsername.isBlank()) {
            throw new IllegalArgumentException("Debtor username cannot be null, blank or empty!");
        }
        if (reason == null || reason.isEmpty() || reason.isBlank()) {
            throw new IllegalArgumentException("Reason cannot be null, blank or empty!");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Debt amount cannot be less than or equal to 0!");
        }
    }

    @Override
    public void addPersonalBaseExpense(String payerUsername,
                                       String participantUsername,
                                       double amount,
                                       String reason) {
        validateArguments(payerUsername, amount, reason);
        if (participantUsername == null || participantUsername.isEmpty() || participantUsername.isBlank()) {
            throw new IllegalArgumentException("Participant username cannot be null, blank or empty!");
        }

        Optional<User> payer = userRepository.getUserByUsername(payerUsername);
        if (payer.isEmpty()) {
            throw new NonExistentUserException("User with username %s does not exist!".formatted(payerUsername));
        }

        Optional<User> participant = userRepository.getUserByUsername(participantUsername);
        if (participant.isEmpty()) {
            throw new NonExistentUserException("User with username %s does not exist!".formatted(participantUsername));
        }
        expensesMap.putIfAbsent(payer.get(), new LinkedHashSet<>());
        Expense expense = new Expense(payer.get(), amount, reason, LocalDateTime.now(), Set.of(participant.get()));
        expensesMap.get(payer.get()).add(expense);
        personalDebtsRepository.increaseDebtBurden(participantUsername, payerUsername, amount / 2.0, reason);
        notificationsRepository.addNotificationForUser(participantUsername,
                "%s noted that they paid %s LV for %s. You owe them %s LV."
                        .formatted(payerUsername, amount, reason, amount / 2.0),
                LocalDateTime.now(), NotificationType.PERSONAL);
        csvProcessor.writeToFile(new ExpenseDTO(expense.payer().username(),
                        expense.amount(), expense.reason(), expense.timestamp(), Set.of(expense.payer().username())));
        logger.info("%s paid %s LV for %s".formatted(payerUsername, amount, reason));
    }

    private void addGroupExpense(FriendGroup group, User payer, double amount, String reason) {
        Set<User> participants = group.participants()
                .stream().filter(user -> !user.equals(payer))
                .collect(Collectors.toSet());

        Expense expense = new Expense(payer, amount, reason, LocalDateTime.now(), participants);
        expensesMap.putIfAbsent(payer, new LinkedHashSet<>());
        expensesMap.get(payer).add(expense);

        double amountPerPerson = amount / (participants.size() + 1);
        participants.forEach(user -> groupDebtsRepository.increaseDebtBurden(user.username(),
                payer.username(), group.name(), amountPerPerson, reason));
        participants.forEach(user -> notificationsRepository.addNotificationForUser(user.username(),
                "%s noted that they paid %s LV in your group %s for %s. You owe them %s LV."
                        .formatted(payer.username(), amount, group.name(), reason, amountPerPerson),
                LocalDateTime.now(), NotificationType.GROUP));
        csvProcessor.writeToFile(new ExpenseDTO(expense.payer().username(),
                expense.amount(),
                expense.reason(),
                expense.timestamp(),
                expense.participants().stream().map(User::username).collect(Collectors.toSet())));
        logger.info("%s paid %s LV for %s in group %s.".formatted(payer.username(), amount, reason, group.name()));
    }

    @Override
    public void addGroupExpense(String payerUsername, String groupName, double amount, String reason) {
        validateArguments(payerUsername, amount, reason);
        if (groupName == null || groupName.isEmpty() || groupName.isBlank()) {
            throw new IllegalArgumentException("Group name cannot be null, blank or empty!");
        }

        Optional<User> payer = userRepository.getUserByUsername(payerUsername);
        if (payer.isEmpty()) {
            throw new NonExistentUserException("User with username %s does not exist!".formatted(payerUsername));
        }

        Optional<FriendGroup> group = friendGroupRepository.getGroup(groupName);
        if (group.isEmpty()) {
            throw new NonExistentUserException("Group with name %s does not exist!".formatted(payerUsername));
        }

        addGroupExpense(group.get(), payer.get(), amount, reason);
    }

    @Override
    public void exportRecent(String username, int count, FileWriter writer) throws IOException {
        if (username == null || username.isEmpty() || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null, blank or empty!");
        }
        if (count <= 0) {
            throw new IllegalArgumentException("Count cannot be less than or equal to 0!");
        }

        List<String> expenses = getExpensesOf(username)
                .stream()
                .sorted(Comparator.comparing(Expense::timestamp).reversed())
                .limit(count)
                .map(e -> "%s: %s [%s] with %s"
                        .formatted(e.timestamp(),
                                e.amount(),
                                e.reason(),
                                String.join(", ", e.participants().stream().map(User::username).toList())))
                .toList();

        try (BufferedWriter bufferedWriter = new BufferedWriter(writer)) {
            String content = String.join(System.lineSeparator(), expenses);
            bufferedWriter.write(content);
            bufferedWriter.write(System.lineSeparator());
            bufferedWriter.flush();
        }
    }
}
