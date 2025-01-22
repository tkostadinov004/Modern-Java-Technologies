package bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.models.Expense;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.DebtsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.ExpensesRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.ImpossibleExpenseException;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.NonExistingUserException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class DefaultExpensesRepository implements ExpensesRepository {
    private final UserRepository userRepository;
    private final DebtsRepository debtsRepository;
    private final Map<User, Set<Expense>> expensesMap;

    public DefaultExpensesRepository(UserRepository userRepository, DebtsRepository debtsRepository) {
        this.userRepository = userRepository;
        this.debtsRepository = debtsRepository;
        this.expensesMap = new HashMap<>();
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

    @Override
    public Set<Expense> getExpensesOf(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null!");
        }
        if (!expensesMap.containsKey(user)) {
            return Set.of();
        }

        return expensesMap.get(user);
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
    public void addExpense(String payerUsername, double amount, String purpose, Set<String> participantsUsernames) {
        validateExpense(payerUsername, amount, purpose, participantsUsernames);

        Optional<User> payer = userRepository.getUserByUsername(payerUsername);
        if (payer.isEmpty()) {
            throw new NonExistingUserException("User with username %s does not exist!".formatted(payerUsername));
        }
        Set<String> nonExistingParticipants = new HashSet<>();
        Set<User> validParticipants = participantsUsernames.stream()
                .map(username -> {
                    Optional<User> user = userRepository.getUserByUsername(username);
                    if (user.isEmpty()) {
                        nonExistingParticipants.add(username);
                    }
                    return user.get();
                }).collect(Collectors.toCollection(HashSet::new));
        if (validParticipants.isEmpty()) {
            throw new ImpossibleExpenseException("There are no valid participants in the expense operation, besides the payer");
        }
        Expense expense = new Expense(payer.get(), amount, purpose, validParticipants);
        expensesMap.putIfAbsent(payer.get(), new HashSet<>());
        expensesMap.get(payer.get()).add(expense);

        double amountPerPerson = amount / (validParticipants.size() + 1);
        validParticipants.forEach(user -> debtsRepository.updateDebt(payer.get(), user, amountPerPerson));
        if (!nonExistingParticipants.isEmpty()) {
            throw new NonExistingUserException("Users with usernames %s do not exist, therefore they're not included in the expense splitting"
                    .formatted(String.join(", ", nonExistingParticipants)));
        }
    }
}
