package bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.data.implementations.PersonalExpensesCsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.dependency.DependencyContainer;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.FriendGroup;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.GroupDebt;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.PersonalExpense;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.PersonalExpensesRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.FriendGroupRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.GroupDebtsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.NotificationsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.PersonalDebtsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.NonExistentUserException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultPersonalExpensesRepositoryTest {
    private static final DependencyContainer dependencyContainer = mock();
    private static final User user1 = new User("user1", "asd", "Test", "Test1");
    private static final User user2 = new User("user2", "asd", "Test", "Test1");
    private static final User user3 = new User("user3", "asd", "Test", "Test1");
    private static final User user4 = new User("user4", "asd", "Test", "Test1");
    private static final FriendGroup group = new FriendGroup("testGroup", Set.of(user1, user2, user3));

    @BeforeAll
    public static void setUp() {
        Logger logger = mock();
        when(dependencyContainer.get(Logger.class))
                .thenReturn(logger);

        NotificationsRepository notificationsRepository = mock();
        when(dependencyContainer.get(NotificationsRepository.class))
                .thenReturn(notificationsRepository);

        PersonalExpensesCsvProcessor csvProcessor = mock();
        when(csvProcessor.readAll())
                .thenReturn(Set.of());
        doAnswer((Answer<Void>) _ -> null)
                .when(csvProcessor).writeToFile(any());
        when(dependencyContainer.get(PersonalExpensesCsvProcessor.class))
                .thenReturn(csvProcessor);

        UserRepository userRepository = mock();
        when(userRepository.getUserByUsername("user1")).thenReturn(Optional.of(user1));
        when(userRepository.getUserByUsername("user2")).thenReturn(Optional.of(user2));
        when(userRepository.getUserByUsername("user3")).thenReturn(Optional.of(user3));
        when(userRepository.getUserByUsername("user4")).thenReturn(Optional.of(user4));
        when(userRepository.containsUser("user1")).thenReturn(true);
        when(userRepository.containsUser("user2")).thenReturn(true);
        when(userRepository.containsUser("user3")).thenReturn(true);
        when(userRepository.containsUser("user4")).thenReturn(true);
        when(dependencyContainer.get(UserRepository.class))
                .thenReturn(userRepository);

        FriendGroupRepository friendGroupRepository = mock();
        when(friendGroupRepository.getGroup("testGroup"))
                .thenReturn(Optional.of(group));
        when(friendGroupRepository.getGroupsOf("user1"))
                .thenReturn(Set.of(group));
        when(friendGroupRepository.getGroupsOf("user2"))
                .thenReturn(Set.of(group));
        when(friendGroupRepository.getGroupsOf("user3"))
                .thenReturn(Set.of(group));
        when(dependencyContainer.get(FriendGroupRepository.class))
                .thenReturn(friendGroupRepository);

        PersonalDebtsRepository personalDebtsRepository = mock();
        when(dependencyContainer.get(PersonalDebtsRepository.class))
                .thenReturn(personalDebtsRepository);
        GroupDebtsRepository groupDebtsRepository = mock();
        when(dependencyContainer.get(GroupDebtsRepository.class))
                .thenReturn(groupDebtsRepository);
    }

    @Test
    public void testGetExpensesOfThrowsOnInvalidUsername() {
        PersonalExpensesRepository expensesRepository = new DefaultPersonalExpensesRepository(dependencyContainer);

        assertThrows(IllegalArgumentException.class, () -> expensesRepository.getExpensesOf(null),
                "getExpensesOf() should throw on null username");
        assertThrows(IllegalArgumentException.class, () -> expensesRepository.getExpensesOf(""),
                "getExpensesOf() should throw on empty username");
        assertThrows(IllegalArgumentException.class, () -> expensesRepository.getExpensesOf("   "),
                "getExpensesOf() should throw on blank username");
    }

    @Test
    public void testGetExpensesOfThrowsOnNonexistentUser() {
        PersonalExpensesRepository expensesRepository = new DefaultPersonalExpensesRepository(dependencyContainer);

        assertThrows(NonExistentUserException.class, () -> expensesRepository.getExpensesOf( "asdasdasd"),
                "getExpensesOf() should throw on non existing user");
    }

    @Test
    public void testGetExpensesOfReturnsEmptySetIfUserHasNoExpenses() {
        PersonalExpensesRepository expensesRepository = new DefaultPersonalExpensesRepository(dependencyContainer);

        assertTrue(expensesRepository.getExpensesOf("user3").isEmpty(),
                "getExpensesOf() should return an empty set if a user has no expenses");
    }

    @Test
    public void testGetExpensesOfReturnsExpensesCorrectly() {
        PersonalExpensesRepository expensesRepository = new DefaultPersonalExpensesRepository(dependencyContainer);
        PersonalExpense expense1 = new PersonalExpense(user1, user2, 100, "test reason", LocalDateTime.now());
        PersonalExpense expense2 = new PersonalExpense(user1, user3, 200, "test reason1", LocalDateTime.now());

        expensesRepository.addExpense(expense1.payer().username(), expense1.debtor().username(), expense1.amount(), expense1.reason(), expense1.timestamp());
        expensesRepository.addExpense(expense2.payer().username(), expense2.debtor().username(), expense2.amount(), expense2.reason(), expense2.timestamp());
        Set<PersonalExpense> expected = Set.of(expense1, expense2);
        Set<PersonalExpense> actual = expensesRepository.getExpensesOf("user1");

        assertTrue(expected.size() == actual.size() &&
                        expected.containsAll(actual),
                "getExpensesOf() should return all personal expenses a user made.");
    }

    @Test
    public void testAddExpenseThrowsOnInvalidPayerUsername() {
        PersonalExpensesRepository expensesRepository = new DefaultPersonalExpensesRepository(dependencyContainer);

        assertThrows(IllegalArgumentException.class, () -> expensesRepository.addExpense(null, "user2", 100, "reason", LocalDateTime.now()),
                "addExpense() should throw on null payer username");
        assertThrows(IllegalArgumentException.class, () -> expensesRepository.addExpense("", "user2", 100, "reason", LocalDateTime.now()),
                "addExpense() should throw on empty payer username");
        assertThrows(IllegalArgumentException.class, () -> expensesRepository.addExpense("   ", "user2", 100, "reason", LocalDateTime.now()),
                "addExpense() should throw on blank payer username");
    }

    @Test
    public void testAddExpenseThrowsOnInvalidDebtorUsername() {
        PersonalExpensesRepository expensesRepository = new DefaultPersonalExpensesRepository(dependencyContainer);

        assertThrows(IllegalArgumentException.class, () -> expensesRepository.addExpense( "user2",null, 100, "reason", LocalDateTime.now()),
                "addExpense() should throw on null debtor username");
        assertThrows(IllegalArgumentException.class, () -> expensesRepository.addExpense("user2", "", 100, "reason", LocalDateTime.now()),
                "addExpense() should throw on empty debtor username");
        assertThrows(IllegalArgumentException.class, () -> expensesRepository.addExpense( "user2", "   ",100, "reason", LocalDateTime.now()),
                "addExpense() should throw on blank debtor username");
    }

    @Test
    public void testAddExpenseThrowsOnNegativeAmount() {
        PersonalExpensesRepository expensesRepository = new DefaultPersonalExpensesRepository(dependencyContainer);

        assertThrows(IllegalArgumentException.class, () -> expensesRepository.addExpense("user1", "user2", -6, "reason", LocalDateTime.now()),
                "addExpense() should throw when amount is negative");
    }

    @Test
    public void testAddExpenseThrowsOnZeroAmount() {
        PersonalExpensesRepository expensesRepository = new DefaultPersonalExpensesRepository(dependencyContainer);

        assertThrows(IllegalArgumentException.class, () -> expensesRepository.addExpense("user1", "user2", 0, "reason", LocalDateTime.now()),
                "addExpense() should throw when amount is zero");
    }

    @Test
    public void testAddExpenseThrowsOnInvalidReason() {
        PersonalExpensesRepository expensesRepository = new DefaultPersonalExpensesRepository(dependencyContainer);

        assertThrows(IllegalArgumentException.class, () -> expensesRepository.addExpense( "user2","user1", 100, null, LocalDateTime.now()),
                "addExpense() should throw on null reason");
        assertThrows(IllegalArgumentException.class, () -> expensesRepository.addExpense("user2", "user1", 100, "", LocalDateTime.now()),
                "addExpense() should throw on empty reason");
        assertThrows(IllegalArgumentException.class, () -> expensesRepository.addExpense( "user2", "user1",100, "   ", LocalDateTime.now()),
                "addExpense() should throw on blank reason");
    }

    @Test
    public void testAddExpenseThrowsOnNullTimestamp() {
        PersonalExpensesRepository expensesRepository = new DefaultPersonalExpensesRepository(dependencyContainer);

        assertThrows(IllegalArgumentException.class, () -> expensesRepository.addExpense("user1", "user2", 22, "reason", null),
                "addExpense() should throw on null timestamp");
    }

    @Test
    public void testAddExpenseOfThrowsOnNonexistentPayer() {
        PersonalExpensesRepository expensesRepository = new DefaultPersonalExpensesRepository(dependencyContainer);

        assertThrows(NonExistentUserException.class, () -> expensesRepository.addExpense( "asdasdasd", "user2", 100, "reason", LocalDateTime.now()),
                "addExpense() should throw on non existing payer");
    }

    @Test
    public void testAddExpenseOfThrowsOnNonexistentDebtor() {
        PersonalExpensesRepository expensesRepository = new DefaultPersonalExpensesRepository(dependencyContainer);

        assertThrows(NonExistentUserException.class, () -> expensesRepository.addExpense( "user2", "adsasdasdas", 100, "reason", LocalDateTime.now()),
                "addExpense() should throw on non existing debtor");
    }
}
