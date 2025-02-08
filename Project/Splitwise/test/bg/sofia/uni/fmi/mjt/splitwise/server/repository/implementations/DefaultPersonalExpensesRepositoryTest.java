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
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserFriendsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.NonExistentUserException;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.NotFriendsException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
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
    private static final DependencyContainer DEPENDENCY_CONTAINER = mock();
    private static final User USER_1 = new User("user1", "asd", "Test", "Test1");
    private static final User USER_2 = new User("user2", "asd", "Test", "Test1");
    private static final User USER_3 = new User("user3", "asd", "Test", "Test1");
    private static final User USER_4 = new User("user4", "asd", "Test", "Test1");

    @BeforeAll
    public static void setUp() {
        Logger logger = mock();
        when(DEPENDENCY_CONTAINER.get(Logger.class))
                .thenReturn(logger);

        NotificationsRepository notificationsRepository = mock();
        when(DEPENDENCY_CONTAINER.get(NotificationsRepository.class))
                .thenReturn(notificationsRepository);

        PersonalExpensesCsvProcessor csvProcessor = mock();
        when(csvProcessor.readAll())
                .thenReturn(Set.of());
        doAnswer((Answer<Void>) _ -> null)
                .when(csvProcessor).writeToFile(any());
        when(DEPENDENCY_CONTAINER.get(PersonalExpensesCsvProcessor.class))
                .thenReturn(csvProcessor);

        UserRepository userRepository = mock();
        when(userRepository.getUserByUsername("user1")).thenReturn(Optional.of(USER_1));
        when(userRepository.getUserByUsername("user2")).thenReturn(Optional.of(USER_2));
        when(userRepository.getUserByUsername("user3")).thenReturn(Optional.of(USER_3));
        when(userRepository.getUserByUsername("user4")).thenReturn(Optional.of(USER_4));
        when(userRepository.containsUser("user1")).thenReturn(true);
        when(userRepository.containsUser("user2")).thenReturn(true);
        when(userRepository.containsUser("user3")).thenReturn(true);
        when(userRepository.containsUser("user4")).thenReturn(true);
        when(DEPENDENCY_CONTAINER.get(UserRepository.class))
                .thenReturn(userRepository);

        UserFriendsRepository userFriendsRepository = mock();
        when(userFriendsRepository.areFriends("user1", "user2")).thenReturn(true);
        when(userFriendsRepository.areFriends("user1", "user3")).thenReturn(true);
        when(DEPENDENCY_CONTAINER.get(UserFriendsRepository.class))
                .thenReturn(userFriendsRepository);

        PersonalDebtsRepository personalDebtsRepository = mock();
        when(DEPENDENCY_CONTAINER.get(PersonalDebtsRepository.class))
                .thenReturn(personalDebtsRepository);
    }

    @Test
    public void testGetExpensesOfThrowsOnInvalidUsername() {
        PersonalExpensesRepository expensesRepository = new DefaultPersonalExpensesRepository(DEPENDENCY_CONTAINER);

        assertThrows(IllegalArgumentException.class, () -> expensesRepository.getExpensesOf(null),
                "getExpensesOf() should throw on null username");
        assertThrows(IllegalArgumentException.class, () -> expensesRepository.getExpensesOf(""),
                "getExpensesOf() should throw on empty username");
        assertThrows(IllegalArgumentException.class, () -> expensesRepository.getExpensesOf("   "),
                "getExpensesOf() should throw on blank username");
    }

    @Test
    public void testGetExpensesOfThrowsOnNonexistentUser() {
        PersonalExpensesRepository expensesRepository = new DefaultPersonalExpensesRepository(DEPENDENCY_CONTAINER);

        assertThrows(NonExistentUserException.class, () -> expensesRepository.getExpensesOf( "asdasdasd"),
                "getExpensesOf() should throw on non existing USER_");
    }

    @Test
    public void testGetExpensesOfReturnsEmptySetIfUserHasNoExpenses() {
        PersonalExpensesRepository expensesRepository = new DefaultPersonalExpensesRepository(DEPENDENCY_CONTAINER);

        assertTrue(expensesRepository.getExpensesOf("user3").isEmpty(),
                "getExpensesOf() should return an empty set if a USER_ has no expenses");
    }

    @Test
    public void testGetExpensesOfReturnsExpensesCorrectly() {
        PersonalExpensesRepository expensesRepository = new DefaultPersonalExpensesRepository(DEPENDENCY_CONTAINER);
        PersonalExpense expense1 = new PersonalExpense(USER_1, USER_2, 100, "test reason", LocalDateTime.now());
        PersonalExpense expense2 = new PersonalExpense(USER_1, USER_3, 200, "test reason1", LocalDateTime.now());

        expensesRepository.addExpense(expense1.payer().username(), expense1.debtor().username(), expense1.amount(), expense1.reason(), expense1.timestamp());
        expensesRepository.addExpense(expense2.payer().username(), expense2.debtor().username(), expense2.amount(), expense2.reason(), expense2.timestamp());
        Set<PersonalExpense> expected = Set.of(expense1, expense2);
        Set<PersonalExpense> actual = expensesRepository.getExpensesOf("user1");

        assertTrue(expected.size() == actual.size() &&
                        expected.containsAll(actual),
                "getExpensesOf() should return all personal expenses a USER_ made.");
    }

    @Test
    public void testAddExpenseThrowsOnInvalidPayerUsername() {
        PersonalExpensesRepository expensesRepository = new DefaultPersonalExpensesRepository(DEPENDENCY_CONTAINER);

        assertThrows(IllegalArgumentException.class, () -> expensesRepository.addExpense(null, "user2", 100, "reason", LocalDateTime.now()),
                "addExpense() should throw on null payer username");
        assertThrows(IllegalArgumentException.class, () -> expensesRepository.addExpense("", "user2", 100, "reason", LocalDateTime.now()),
                "addExpense() should throw on empty payer username");
        assertThrows(IllegalArgumentException.class, () -> expensesRepository.addExpense("   ", "user2", 100, "reason", LocalDateTime.now()),
                "addExpense() should throw on blank payer username");
    }

    @Test
    public void testAddExpenseThrowsOnInvalidDebtorUsername() {
        PersonalExpensesRepository expensesRepository = new DefaultPersonalExpensesRepository(DEPENDENCY_CONTAINER);

        assertThrows(IllegalArgumentException.class, () -> expensesRepository.addExpense( "user2",null, 100, "reason", LocalDateTime.now()),
                "addExpense() should throw on null debtor username");
        assertThrows(IllegalArgumentException.class, () -> expensesRepository.addExpense("user2", "", 100, "reason", LocalDateTime.now()),
                "addExpense() should throw on empty debtor username");
        assertThrows(IllegalArgumentException.class, () -> expensesRepository.addExpense( "user2", "   ",100, "reason", LocalDateTime.now()),
                "addExpense() should throw on blank debtor username");
    }
    
    @Test
    public void testAddExpenseThrowsWhenDebtorAndParticipantAreTheSame() {
        PersonalExpensesRepository expensesRepository = new DefaultPersonalExpensesRepository(DEPENDENCY_CONTAINER);

        assertThrows(IllegalArgumentException.class, () -> expensesRepository.addExpense( "user2","user2", 100, "reason", LocalDateTime.now()),
                "addExpense() should throw when debtor and participant are the same");
    }

    @Test
    public void testAddExpenseThrowsOnNegativeAmount() {
        PersonalExpensesRepository expensesRepository = new DefaultPersonalExpensesRepository(DEPENDENCY_CONTAINER);

        assertThrows(IllegalArgumentException.class, () -> expensesRepository.addExpense("user1", "user2", -6, "reason", LocalDateTime.now()),
                "addExpense() should throw when amount is negative");
    }

    @Test
    public void testAddExpenseThrowsOnZeroAmount() {
        PersonalExpensesRepository expensesRepository = new DefaultPersonalExpensesRepository(DEPENDENCY_CONTAINER);

        assertThrows(IllegalArgumentException.class, () -> expensesRepository.addExpense("user1", "user2", 0, "reason", LocalDateTime.now()),
                "addExpense() should throw when amount is zero");
    }

    @Test
    public void testAddExpenseThrowsOnInvalidReason() {
        PersonalExpensesRepository expensesRepository = new DefaultPersonalExpensesRepository(DEPENDENCY_CONTAINER);

        assertThrows(IllegalArgumentException.class, () -> expensesRepository.addExpense( "user2","user1", 100, null, LocalDateTime.now()),
                "addExpense() should throw on null reason");
        assertThrows(IllegalArgumentException.class, () -> expensesRepository.addExpense("user2", "user1", 100, "", LocalDateTime.now()),
                "addExpense() should throw on empty reason");
        assertThrows(IllegalArgumentException.class, () -> expensesRepository.addExpense( "user2", "user1",100, "   ", LocalDateTime.now()),
                "addExpense() should throw on blank reason");
    }

    @Test
    public void testAddExpenseThrowsOnNullTimestamp() {
        PersonalExpensesRepository expensesRepository = new DefaultPersonalExpensesRepository(DEPENDENCY_CONTAINER);

        assertThrows(IllegalArgumentException.class, () -> expensesRepository.addExpense("user1", "user2", 22, "reason", null),
                "addExpense() should throw on null timestamp");
    }

    @Test
    public void testAddExpenseOfThrowsOnNonexistentPayer() {
        PersonalExpensesRepository expensesRepository = new DefaultPersonalExpensesRepository(DEPENDENCY_CONTAINER);

        assertThrows(NonExistentUserException.class, () -> expensesRepository.addExpense( "asdasdasd", "user2", 100, "reason", LocalDateTime.now()),
                "addExpense() should throw on non existing payer");
    }

    @Test
    public void testAddExpenseOfThrowsOnNonexistentDebtor() {
        PersonalExpensesRepository expensesRepository = new DefaultPersonalExpensesRepository(DEPENDENCY_CONTAINER);

        assertThrows(NonExistentUserException.class, () -> expensesRepository.addExpense( "user2", "adsasdasdas", 100, "reason", LocalDateTime.now()),
                "addExpense() should throw on non existing debtor");
    }

    @Test
    public void testAddExpenseOfThrowsIfUsersAreNotFriends() {
        PersonalExpensesRepository expensesRepository = new DefaultPersonalExpensesRepository(DEPENDENCY_CONTAINER);

        assertThrows(NotFriendsException.class, () -> expensesRepository.addExpense( "user2", "user3", 100, "reason", LocalDateTime.now()),
                "addExpense() should throw when debtor and participant are not friends");
    }

    @Test
    public void testExportRecentThrowsOnInvalidUsername() {
        PersonalExpensesRepository expensesRepository = new DefaultPersonalExpensesRepository(DEPENDENCY_CONTAINER);

        assertThrows(IllegalArgumentException.class, () -> expensesRepository.exportRecent( null,50, new BufferedWriter(new StringWriter())),
                "exportRecent() should throw on null username");
        assertThrows(IllegalArgumentException.class, () -> expensesRepository.exportRecent("", 50,   new BufferedWriter(new StringWriter())),
                "exportRecent() should throw on empty username");
        assertThrows(IllegalArgumentException.class, () -> expensesRepository.exportRecent( "   ", 50,  new BufferedWriter(new StringWriter())),
                "exportRecent() should throw on blank username");
    }

    @Test
    public void testExportRecentThrowsOnNegativeCount() {
        PersonalExpensesRepository expensesRepository = new DefaultPersonalExpensesRepository(DEPENDENCY_CONTAINER);

        assertThrows(IllegalArgumentException.class, () -> expensesRepository.exportRecent( "user1",-1, new BufferedWriter(new StringWriter())),
                "exportRecent() should throw on negative count");
    }

    @Test
    public void testExportRecentThrowsOnZeroCount() {
        PersonalExpensesRepository expensesRepository = new DefaultPersonalExpensesRepository(DEPENDENCY_CONTAINER);

        assertThrows(IllegalArgumentException.class, () -> expensesRepository.exportRecent( "user1",0, new BufferedWriter(new StringWriter())),
                "exportRecent() should throw on negative count");
    }

    @Test
    public void testExportRecentThrowsOnNullWriter() {
        PersonalExpensesRepository expensesRepository = new DefaultPersonalExpensesRepository(DEPENDENCY_CONTAINER);

        assertThrows(IllegalArgumentException.class, () -> expensesRepository.exportRecent( "user1",50, null),
                "exportRecent() should throw on null writer");
    }

    @Test
    public void testExportRecentExportsCorrectly() throws IOException {
        PersonalExpensesRepository expensesRepository = new DefaultPersonalExpensesRepository(DEPENDENCY_CONTAINER);
        LocalDateTime now = LocalDateTime.now();
        expensesRepository.addExpense("user1", "user2", 100, "bananas", now.minusDays(10));
        expensesRepository.addExpense("user1", "user2", 100, "bananas", now.minusDays(20));
        expensesRepository.addExpense("user1", "user3", 50, "another cost", now);

        StringWriter stringWriter = new StringWriter();
        BufferedWriter bufferedWriter = new BufferedWriter(stringWriter);

        expensesRepository.exportRecent("user1", 2, bufferedWriter);

        String expected = "%s: 50.0 [another cost] with %s".formatted(now, USER_3) + System.lineSeparator() +
                        "%s: 100.0 [bananas] with %s".formatted(now.minusDays(10), USER_2) + System.lineSeparator();

        assertEquals(expected, stringWriter.toString(),
                "Expenses should be sorted in descending order by timestamp");
    }
}
