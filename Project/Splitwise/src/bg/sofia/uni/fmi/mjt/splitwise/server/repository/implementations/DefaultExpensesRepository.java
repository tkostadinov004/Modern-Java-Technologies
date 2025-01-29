package bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.models.Expense;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.FriendGroup;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.NotificationType;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.FriendGroupRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.GroupDebtsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.NotificationsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.PersonalDebtsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.ExpensesRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.NonExistingUserException;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import java.io.IOException;
import java.io.Writer;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefaultExpensesRepository implements ExpensesRepository {
    private final UserRepository userRepository;
    private final FriendGroupRepository friendGroupRepository;
    private final PersonalDebtsRepository personalDebtsRepository;
    private final GroupDebtsRepository groupDebtsRepository;
    private final NotificationsRepository notificationsRepository;
    private final Map<User, Set<Expense>> expensesMap;

    public DefaultExpensesRepository(UserRepository userRepository, FriendGroupRepository friendGroupRepository, PersonalDebtsRepository personalDebtsRepository, GroupDebtsRepository groupDebtsRepository, NotificationsRepository notificationsRepository) {
        this.userRepository = userRepository;
        this.friendGroupRepository = friendGroupRepository;
        this.personalDebtsRepository = personalDebtsRepository;
        this.groupDebtsRepository = groupDebtsRepository;
        this.notificationsRepository = notificationsRepository;
        this.expensesMap = new HashMap<>();
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
            throw new NonExistingUserException("User with username %s does not exist!".formatted(username));
        }

        return getExpensesOf(user.get());
    }

    private void validateExpense(String payerUsername, double amount, String purpose, Set<String> participantsUsernames) {
        if (payerUsername == null || payerUsername.isEmpty() || payerUsername.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null, blank or empty!");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Expense amount cannot be less than or equal to 0!");
        }
        if (purpose == null || purpose.isEmpty() || purpose.isBlank()) {
            throw new IllegalArgumentException("Expense purpose cannot be null, blank or empty!");
        }
        if (participantsUsernames == null) {
            throw new IllegalArgumentException("The set of participants cannot be null!");
        }
    }

    @Override
    public void addPersonalBaseExpense(String payerUsername, String participantUsername, double amount, String purpose) {
        if (payerUsername == null || payerUsername.isEmpty() || payerUsername.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null, blank or empty!");
        }
        if (participantUsername == null || participantUsername.isEmpty() || participantUsername.isBlank()) {
            throw new IllegalArgumentException("Participant username cannot be null, blank or empty!");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Expense amount cannot be less than or equal to 0!");
        }
        if (purpose == null || purpose.isEmpty() || purpose.isBlank()) {
            throw new IllegalArgumentException("Expense purpose cannot be null, blank or empty!");
        }

        Optional<User> payer = userRepository.getUserByUsername(payerUsername);
        if (payer.isEmpty()) {
            throw new NonExistingUserException("User with username %s does not exist!".formatted(payerUsername));
        }

        Optional<User> participant = userRepository.getUserByUsername(participantUsername);
        if (participant.isEmpty()) {
            throw new NonExistingUserException("User with username %s does not exist!".formatted(participantUsername));
        }
        expensesMap.putIfAbsent(payer.get(), new LinkedHashSet<>());
        expensesMap.get(payer.get()).add(new Expense(payer.get(), amount, purpose, LocalDateTime.now(), Set.of(participant.get())));
        personalDebtsRepository.updateDebt(participantUsername, payerUsername, amount / 2.0, purpose);
        notificationsRepository.addNotificationForUser(participantUsername,
                "%s noted that they paid %s LV for %s. You owe them %s LV.".formatted(payerUsername, amount, purpose, amount / 2.0),
                LocalDateTime.now(), NotificationType.PERSONAL);
    }

    @Override
    public void addGroupExpense(String payerUsername, String groupName, double amount, String purpose) {
        if (payerUsername == null || payerUsername.isEmpty() || payerUsername.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null, blank or empty!");
        }
        if (groupName == null || groupName.isEmpty() || groupName.isBlank()) {
            throw new IllegalArgumentException("Group name cannot be null, blank or empty!");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Expense amount cannot be less than or equal to 0!");
        }
        if (purpose == null || purpose.isEmpty() || purpose.isBlank()) {
            throw new IllegalArgumentException("Expense purpose cannot be null, blank or empty!");
        }

        Optional<User> payer = userRepository.getUserByUsername(payerUsername);
        if (payer.isEmpty()) {
            throw new NonExistingUserException("User with username %s does not exist!".formatted(payerUsername));
        }

        Optional<FriendGroup> group = friendGroupRepository.getGroup(groupName);
        if (group.isEmpty()) {
            throw new NonExistingUserException("Group with name %s does not exist!".formatted(payerUsername));
        }

        Set<User> participants = group.get().participants()
                .stream().filter(user -> !user.equals(payer.get()))
                .collect(Collectors.toSet());

        Expense expense = new Expense(payer.get(), amount, purpose, LocalDateTime.now(), participants);
        expensesMap.putIfAbsent(payer.get(), new LinkedHashSet<>());
        expensesMap.get(payer.get()).add(expense);

        double amountPerPerson = amount / (participants.size() + 1);
        participants.forEach(user -> groupDebtsRepository.updateDebt(user.username(), payerUsername, groupName, amountPerPerson, purpose));
        participants.forEach(user -> notificationsRepository.addNotificationForUser(user.username(),
                "%s noted that they paid %s LV in your group %s for %s. You owe them %s LV.".formatted(payerUsername, amount, groupName, purpose, amountPerPerson),
                LocalDateTime.now(), NotificationType.GROUP));
    }

    @Override
    public void exportRecent(String username, int count, Writer writer) throws IOException {
        StatefulBeanToCsv<Expense> beanToCsv = new StatefulBeanToCsvBuilder<Expense>(writer).build();
        Stream<Expense> expensesStream = getExpensesOf(username)
                .stream()
                .sorted(Comparator.comparing(Expense::timestamp).reversed())
                .limit(count);

        try {
            beanToCsv.write(expensesStream);
        } catch (CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
            throw new IOException(e);
        }
    }
}
