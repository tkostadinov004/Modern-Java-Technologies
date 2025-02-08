package bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.data.CsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.data.implementations.GroupExpensesCsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.dependency.DependencyContainer;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.FriendGroup;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.GroupExpense;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.NotificationType;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.dto.GroupExpenseDTO;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.FriendGroupRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.GroupDebtsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.GroupExpensesRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.NotificationsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.NonExistentUserException;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.NonExistentFriendGroupException;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.UserNotInGroupException;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations.converter.DataConverter;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations.converter.GroupExpensesConverter;

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

public class DefaultGroupExpensesRepository implements GroupExpensesRepository {
    private final Logger logger;
    private final CsvProcessor<GroupExpenseDTO> csvProcessor;
    private final UserRepository userRepository;
    private final FriendGroupRepository friendGroupRepository;
    private final GroupDebtsRepository groupDebtsRepository;
    private final NotificationsRepository notificationsRepository;
    private final Map<User, Set<GroupExpense>> expensesMap;

    public DefaultGroupExpensesRepository(DependencyContainer dependencyContainer) {
        this.logger = dependencyContainer.get(Logger.class);
        this.csvProcessor = dependencyContainer.get(GroupExpensesCsvProcessor.class);
        this.userRepository = dependencyContainer.get(UserRepository.class);
        this.friendGroupRepository = dependencyContainer.get(FriendGroupRepository.class);
        this.groupDebtsRepository = dependencyContainer.get(GroupDebtsRepository.class);
        this.notificationsRepository = dependencyContainer.get(NotificationsRepository.class);

        DataConverter<Map<User, Set<GroupExpense>>, GroupExpense, GroupExpenseDTO> converter =
                new GroupExpensesConverter(csvProcessor, userRepository, friendGroupRepository);
        this.expensesMap = new ConcurrentHashMap<>(converter.populate(Collectors.groupingBy(GroupExpense::payer,
                Collectors.mapping(expense -> expense,
                        Collectors.toCollection(() -> Collections.synchronizedSet(new HashSet<>()))))));
    }

    @Override
    public Set<GroupExpense> getExpensesOf(String username) {
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

    private void addExpense(FriendGroup group,
                            User payer,
                            double amount,
                            String reason,
                            LocalDateTime timestamp) {
        synchronized (group) {
            Set<User> participants = group.participants()
                    .stream().filter(user -> !user.equals(payer))
                    .collect(Collectors.toSet());

            GroupExpense expense = new GroupExpense(payer, amount, reason, group, timestamp);
            expensesMap.putIfAbsent(payer, Collections.synchronizedSet(new HashSet<>()));
            expensesMap.get(payer).add(expense);

            double amountPerPerson = amount / (participants.size() + 1);
            participants.forEach(user -> groupDebtsRepository.increaseDebtBurden(user.username(),
                    payer.username(), group.name(), amountPerPerson, reason));
            participants.forEach(user -> notificationsRepository.addNotificationForUser(user.username(),
                    "%s noted that they paid %s LV in your group %s for %s. You owe them %s LV."
                            .formatted(payer.username(), amount, group.name(), reason, amountPerPerson),
                    timestamp, NotificationType.GROUP));

            csvProcessor.writeToFile(new GroupExpenseDTO(expense.payer().username(),
                expense.amount(),
                expense.reason(),
                expense.group().name(),
                expense.timestamp()));
            logger.info("%s paid %s LV for %s in group %s.".formatted(payer.username(), amount, reason, group.name()));
        }
    }

    private void validateArguments(String payerUsername,
                                   String groupName,
                                   double amount,
                                   String reason,
                                   LocalDateTime timestamp) {
        if (payerUsername == null || payerUsername.isEmpty() || payerUsername.isBlank()) {
            throw new IllegalArgumentException("Payer username cannot be null, blank or empty!");
        }
        if (groupName == null || groupName.isEmpty() || groupName.isBlank()) {
            throw new IllegalArgumentException("Group name cannot be null, blank or empty!");
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
                           String groupName,
                           double amount,
                           String reason,
                           LocalDateTime timestamp) {
        validateArguments(payerUsername, groupName, amount, reason, timestamp);

        Optional<User> payer = userRepository.getUserByUsername(payerUsername);
        if (payer.isEmpty()) {
            throw new NonExistentUserException("User with username %s does not exist!".formatted(payerUsername));
        }

        Optional<FriendGroup> group = friendGroupRepository.getGroup(groupName);
        if (group.isEmpty()) {
            throw new NonExistentFriendGroupException("Group with name %s does not exist!".formatted(payerUsername));
        }

        if (!group.get().participants().contains(payer.get())) {
            throw new UserNotInGroupException("User %s is not in group %s!"
                    .formatted(payerUsername, groupName));
        }

        addExpense(group.get(), payer.get(), amount, reason, timestamp);
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
                    .sorted(Comparator.comparing(GroupExpense::timestamp).reversed())
                    .limit(count)
                    .map(e -> "%s: %s [%s] in group %s"
                            .formatted(e.timestamp(),
                                    e.amount(),
                                    e.reason(),
                                    e.group()))
                    .toList();

            String content = String.join(System.lineSeparator(), expenses);
            writer.write(content);
            writer.write(System.lineSeparator());
            writer.flush();
        }
    }
}
