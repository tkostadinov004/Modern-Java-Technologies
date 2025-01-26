package bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.models.Expense;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.FriendGroup;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.FriendGroupRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.GroupDebtsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.PersonalDebtsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.ExpensesRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.NonExistingUserException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class DefaultExpensesRepository implements ExpensesRepository {
    private final UserRepository userRepository;
    private final FriendGroupRepository friendGroupRepository;
    private final PersonalDebtsRepository personalDebtsRepository;
    private final GroupDebtsRepository groupDebtsRepository;
    private final Map<User, Set<Expense>> expensesMap;

    public DefaultExpensesRepository(UserRepository userRepository, FriendGroupRepository friendGroupRepository, PersonalDebtsRepository personalDebtsRepository, GroupDebtsRepository groupDebtsRepository) {
        this.userRepository = userRepository;
        this.friendGroupRepository = friendGroupRepository;
        this.personalDebtsRepository = personalDebtsRepository;
        this.groupDebtsRepository = groupDebtsRepository;
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
            throw new NonExistingUserException("User with username %s does not exist!".formatted(payerUsername));
        }
        expensesMap.putIfAbsent(payer.get(), new HashSet<>());
        expensesMap.get(payer.get()).add(new Expense(payer.get(), amount, purpose, Set.of(participant.get())));
        personalDebtsRepository.updateDebt(participantUsername, payerUsername, amount / 2.0, purpose);
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

        Expense expense = new Expense(payer.get(), amount, purpose, participants);
        expensesMap.putIfAbsent(payer.get(), new HashSet<>());
        expensesMap.get(payer.get()).add(expense);

        double amountPerPerson = amount / (group.get().participants().size() + 1);
        participants.forEach(user -> personalDebtsRepository.updateDebt(user.username(), payer.get().username(), amountPerPerson, purpose));
    }
}
