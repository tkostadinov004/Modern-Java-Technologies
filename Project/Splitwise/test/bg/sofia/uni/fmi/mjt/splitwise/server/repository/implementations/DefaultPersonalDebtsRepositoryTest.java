package bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.data.implementations.ExpensesCsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.data.implementations.PersonalDebtsCsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.dependency.DependencyContainer;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.Notification;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.NotificationType;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.PersonalDebt;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.dto.FriendshipRelationDTO;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.NotificationsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.PersonalDebtsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.NonExistentDebtException;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.NonExistentUserException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DefaultPersonalDebtsRepositoryTest {
    private static final DependencyContainer dependencyContainer = mock();
    private static final User user1 = new User("user1", "asd", "Test", "Test1");
    private static final User user2 = new User("user2", "asd", "Test", "Test1");
    private static final User user3 = new User("user3", "asd", "Test", "Test1");

    @BeforeAll
    public static void setUp() {
        NotificationsRepository notificationsRepository = mock();
        when(dependencyContainer.get(NotificationsRepository.class))
                .thenReturn(notificationsRepository);

        PersonalDebtsCsvProcessor csvProcessor = mock();
        when(csvProcessor.readAll())
                .thenReturn(Set.of());
        doAnswer((Answer<Void>) _ -> null)
                .when(csvProcessor).writeToFile(any());
        when(dependencyContainer.get(PersonalDebtsCsvProcessor.class))
                .thenReturn(csvProcessor);

        UserRepository userRepository = mock();
        when(userRepository.getUserByUsername("user1")).thenReturn(Optional.of(user1));
        when(userRepository.getUserByUsername("user2")).thenReturn(Optional.of(user2));
        when(userRepository.getUserByUsername("user3")).thenReturn(Optional.of(user3));
        when(userRepository.containsUser("user1")).thenReturn(true);
        when(userRepository.containsUser("user2")).thenReturn(true);
        when(userRepository.containsUser("user3")).thenReturn(true);
        when(dependencyContainer.get(UserRepository.class))
                .thenReturn(userRepository);
    }

    @Test
    public void testGetDebtsOfThrowsOnInvalidUsername() {
        PersonalDebtsRepository personalDebtsRepository = new DefaultPersonalDebtsRepository(dependencyContainer);

        assertThrows(IllegalArgumentException.class, () -> personalDebtsRepository.getDebtsOf(null),
                "getDebtsOf() should throw on null username");
        assertThrows(IllegalArgumentException.class, () -> personalDebtsRepository.getDebtsOf(""),
                "getDebtsOf() should throw on empty username");
        assertThrows(IllegalArgumentException.class, () -> personalDebtsRepository.getDebtsOf("   "),
                "getDebtsOf() should throw on blank username");
    }

    @Test
    public void testGetDebtsOfThrowsOnNonexistentUser() {
        PersonalDebtsRepository personalDebtsRepository = new DefaultPersonalDebtsRepository(dependencyContainer);;

        assertThrows(NonExistentUserException.class, () -> personalDebtsRepository.getDebtsOf( "asdasdasd"),
                "getExpensesOf() should throw on non existing user");
    }

    @Test
    public void testGetDebtsOfReturnsDebtsCorrectly() {
        PersonalDebtsRepository personalDebtsRepository = new DefaultPersonalDebtsRepository(dependencyContainer);
        personalDebtsRepository.increaseDebtBurden("user1", "user2", 100, "test reason");
        personalDebtsRepository.increaseDebtBurden("user2", "user1", 500, "another test");

        Set<PersonalDebt> expected = Set.of(new PersonalDebt(user1, user2, 100, "test reason"),
                new PersonalDebt(user2, user1, 500, "another test"));
        Set<PersonalDebt> actual = personalDebtsRepository.getDebtsOf("user1");

        assertTrue(expected.size() == actual.size() && expected.containsAll(actual) && actual.containsAll(expected),
                "getDebtsOf() should return all personal debts in which the user is a debtor or a recipient.");
    }

    @Test
    public void testLowerDebtBurdenThrowsOnInvalidDebtorUsername() {
        PersonalDebtsRepository personalDebtsRepository = new DefaultPersonalDebtsRepository(dependencyContainer);

        assertThrows(IllegalArgumentException.class,
                () -> personalDebtsRepository.lowerDebtBurden(null, "asd", 50, "asd"),
                "lowerDebtBurden() should throw on null debtor username");
        assertThrows(IllegalArgumentException.class,
                () -> personalDebtsRepository.lowerDebtBurden("", "asd", 50, "asd"),
                "lowerDebtBurden() should throw on empty debtor username");
        assertThrows(IllegalArgumentException.class,
                () -> personalDebtsRepository.lowerDebtBurden("   ", "asd", 50, "asd"),
                "lowerDebtBurden() should throw on blank debtor username");
    }

    @Test
    public void testLowerDebtBurdenThrowsOnInvalidRecipientUsername() {
        PersonalDebtsRepository personalDebtsRepository = new DefaultPersonalDebtsRepository(dependencyContainer);

        assertThrows(IllegalArgumentException.class,
                () -> personalDebtsRepository.lowerDebtBurden("asd", null , 50, "asd"),
                "lowerDebtBurden() should throw on null recipient username");
        assertThrows(IllegalArgumentException.class,
                () -> personalDebtsRepository.lowerDebtBurden("asd", "" ,50, "asd"),
                "lowerDebtBurden() should throw on empty recipient username");
        assertThrows(IllegalArgumentException.class,
                () -> personalDebtsRepository.lowerDebtBurden("asd", "   ", 50, "asd"),
                "lowerDebtBurden() should throw on blank recipient username");
    }

    @Test
    public void testLowerDebtBurdenThrowsOnNegativeAmount() {
        PersonalDebtsRepository personalDebtsRepository = new DefaultPersonalDebtsRepository(dependencyContainer);

        assertThrows(IllegalArgumentException.class,
                () -> personalDebtsRepository.lowerDebtBurden("asd", "asdd" , -1, "asd"),
                "lowerDebtBurden() should throw on null debtor username");
    }

    @Test
    public void testLowerDebtBurdenThrowsOnZeroAmount() {
        PersonalDebtsRepository personalDebtsRepository = new DefaultPersonalDebtsRepository(dependencyContainer);

        assertThrows(IllegalArgumentException.class,
                () -> personalDebtsRepository.lowerDebtBurden("asd", "asdd" , 0, "asd"),
                "lowerDebtBurden() should throw on null debtor username");
    }

    @Test
    public void testLowerDebtBurdenThrowsOnInvalidReason() {
        PersonalDebtsRepository personalDebtsRepository = new DefaultPersonalDebtsRepository(dependencyContainer);

        assertThrows(IllegalArgumentException.class,
                () -> personalDebtsRepository.lowerDebtBurden("asd", "asdd" , 50, null),
                "lowerDebtBurden() should throw on null reason");
        assertThrows(IllegalArgumentException.class,
                () -> personalDebtsRepository.lowerDebtBurden("asd", "asdd" ,50, ""),
                "lowerDebtBurden() should throw on empty reason");
        assertThrows(IllegalArgumentException.class,
                () -> personalDebtsRepository.lowerDebtBurden("asd", "asdd", 50, "   "),
                "lowerDebtBurden() should throw on blank reason");
    }

    @Test
    public void testLowerDebtBurdenThrowsOnNonexistentDebtor() {
        PersonalDebtsRepository personalDebtsRepository = new DefaultPersonalDebtsRepository(dependencyContainer);;

        assertThrows(NonExistentUserException.class, () -> personalDebtsRepository.lowerDebtBurden( "asdasdasd", "user1", 50, "asdasd"),
                "lowerDebtBurden() should throw on nonexisting debtor");
    }

    @Test
    public void testLowerDebtBurdenThrowsOnNonexistentRecipient() {
        PersonalDebtsRepository personalDebtsRepository = new DefaultPersonalDebtsRepository(dependencyContainer);;

        assertThrows(NonExistentUserException.class, () -> personalDebtsRepository.lowerDebtBurden( "user1", "asdasdasd", 50, "asdasd"),
                "lowerDebtBurden() should throw on nonexisting recipient");
    }

    @Test
    public void testLowerDebtBurdenThrowsOnEqualDebtorAndRecipient() {
        PersonalDebtsRepository personalDebtsRepository = new DefaultPersonalDebtsRepository(dependencyContainer);;

        assertThrows(IllegalArgumentException.class, () -> personalDebtsRepository.lowerDebtBurden( "user1", "user1", 50, "asdasd"),
                "lowerDebtBurden() should throw when debtor and recipient are equal");
    }

    @Test
    public void testLowerDebtBurdenThrowsOnNonexistentDebt() {
        PersonalDebtsRepository personalDebtsRepository = new DefaultPersonalDebtsRepository(dependencyContainer);

        assertThrows(NonExistentDebtException.class, () -> personalDebtsRepository.lowerDebtBurden( "user1", "user2", 50, "asdasd"),
                "lowerDebtBurden() should throw when debt doesn't exist");
    }

    @Test
    public void testLowerDebtBurdenRemovesDebtIfCompletelyPaidOff() {
        PersonalDebtsRepository personalDebtsRepository = new DefaultPersonalDebtsRepository(dependencyContainer);;
        personalDebtsRepository.increaseDebtBurden("user1", "user2", 100, "test reason");

        personalDebtsRepository.lowerDebtBurden("user1", "user2", 100, "test reason");

        Optional<PersonalDebt> debt = personalDebtsRepository.getDebtsOf("user1")
                        .stream().filter(d -> d.recipient().equals(user2) && d.reason().equals("test reason"))
                        .findFirst();
        assertTrue(debt.isEmpty(),
                "If a debt is completely paid off, it should be deleted from the repository.");
    }

    @Test
    public void testLowerDebtBurdenLowersDebtAmount() {
        PersonalDebtsRepository personalDebtsRepository = new DefaultPersonalDebtsRepository(dependencyContainer);;
        personalDebtsRepository.increaseDebtBurden("user1", "user2", 100, "test reason");

        personalDebtsRepository.lowerDebtBurden("user1", "user2", 30, "test reason");

        Optional<PersonalDebt> debt = personalDebtsRepository.getDebtsOf("user1")
                        .stream().filter(d -> d.recipient().equals(user2) && d.reason().equals("test reason"))
                        .findFirst();
        assertTrue(debt.isPresent(),
                "Even though its amount got lowered, the debt should still be present.");
        assertEquals(70, debt.get().amount(),
                "Debt's amount should get lowered.");
    }

    @Test
    public void testLowerDebtBurdenSendsNotification() {
        NotificationsRepository notificationsRepository = mock();
        when(dependencyContainer.get(NotificationsRepository.class))
                .thenReturn(notificationsRepository);

        PersonalDebtsRepository personalDebtsRepository = new DefaultPersonalDebtsRepository(dependencyContainer);;
        personalDebtsRepository.increaseDebtBurden("user1", "user2", 100, "test reason");

        personalDebtsRepository.lowerDebtBurden("user1", "user2", 30, "test reason");

        verify(notificationsRepository, times(1))
                .addNotificationForUser("user1",
                        "user2 approved your payment of 30.0 LV for test reason. You now owe them 70.0 LV.",
                        NotificationType.PERSONAL);
    }

    @Test
    public void testIncreaseDebtBurdenThrowsOnInvalidDebtorUsername() {
        PersonalDebtsRepository personalDebtsRepository = new DefaultPersonalDebtsRepository(dependencyContainer);

        assertThrows(IllegalArgumentException.class,
                () -> personalDebtsRepository.increaseDebtBurden(null, "asd", 50, "asd"),
                "increaseDebtBurden() should throw on null debtor username");
        assertThrows(IllegalArgumentException.class,
                () -> personalDebtsRepository.increaseDebtBurden("", "asd", 50, "asd"),
                "increaseDebtBurden() should throw on empty debtor username");
        assertThrows(IllegalArgumentException.class,
                () -> personalDebtsRepository.increaseDebtBurden("   ", "asd", 50, "asd"),
                "increaseDebtBurden() should throw on blank debtor username");
    }

    @Test
    public void testIncreaseDebtBurdenThrowsOnInvalidRecipientUsername() {
        PersonalDebtsRepository personalDebtsRepository = new DefaultPersonalDebtsRepository(dependencyContainer);

        assertThrows(IllegalArgumentException.class,
                () -> personalDebtsRepository.increaseDebtBurden("asd", null , 50, "asd"),
                "increaseDebtBurden() should throw on null recipient username");
        assertThrows(IllegalArgumentException.class,
                () -> personalDebtsRepository.increaseDebtBurden("asd", "" ,50, "asd"),
                "increaseDebtBurden() should throw on empty recipient username");
        assertThrows(IllegalArgumentException.class,
                () -> personalDebtsRepository.increaseDebtBurden("asd", "   ", 50, "asd"),
                "increaseDebtBurden() should throw on blank recipient username");
    }

    @Test
    public void testIncreaseDebtBurdenThrowsOnNegativeAmount() {
        PersonalDebtsRepository personalDebtsRepository = new DefaultPersonalDebtsRepository(dependencyContainer);

        assertThrows(IllegalArgumentException.class,
                () -> personalDebtsRepository.increaseDebtBurden("asd", "asdd" , -1, "asd"),
                "increaseDebtBurden() should throw on null debtor username");
    }

    @Test
    public void testIncreaseDebtBurdenThrowsOnZeroAmount() {
        PersonalDebtsRepository personalDebtsRepository = new DefaultPersonalDebtsRepository(dependencyContainer);

        assertThrows(IllegalArgumentException.class,
                () -> personalDebtsRepository.increaseDebtBurden("asd", "asdd" , 0, "asd"),
                "increaseDebtBurden() should throw on null debtor username");
    }

    @Test
    public void tesIncreaseDebtBurdenThrowsOnInvalidReason() {
        PersonalDebtsRepository personalDebtsRepository = new DefaultPersonalDebtsRepository(dependencyContainer);

        assertThrows(IllegalArgumentException.class,
                () -> personalDebtsRepository.increaseDebtBurden("asd", "asdd" , 50, null),
                "increaseDebtBurden() should throw on null reason");
        assertThrows(IllegalArgumentException.class,
                () -> personalDebtsRepository.increaseDebtBurden("asd", "asdd" ,50, ""),
                "increaseDebtBurden() should throw on empty reason");
        assertThrows(IllegalArgumentException.class,
                () -> personalDebtsRepository.increaseDebtBurden("asd", "asdd", 50, "   "),
                "increaseDebtBurden() should throw on blank reason");
    }

    @Test
    public void testIncreaseDebtBurdenThrowsOnNonexistentDebtor() {
        PersonalDebtsRepository personalDebtsRepository = new DefaultPersonalDebtsRepository(dependencyContainer);;

        assertThrows(NonExistentUserException.class, () -> personalDebtsRepository.increaseDebtBurden( "asdasdasd", "user1", 50, "asdasd"),
                "increaseDebtBurden() should throw on nonexisting debtor");
    }

    @Test
    public void testIncreaseDebtBurdenThrowsOnNonexistentRecipient() {
        PersonalDebtsRepository personalDebtsRepository = new DefaultPersonalDebtsRepository(dependencyContainer);;

        assertThrows(NonExistentUserException.class, () -> personalDebtsRepository.increaseDebtBurden( "user1", "asdasdasd", 50, "asdasd"),
                "increaseDebtBurden() should throw on nonexisting recipient");
    }

    @Test
    public void testIncreaseDebtBurdenThrowsOnEqualDebtorAndRecipient() {
        PersonalDebtsRepository personalDebtsRepository = new DefaultPersonalDebtsRepository(dependencyContainer);;

        assertThrows(IllegalArgumentException.class, () -> personalDebtsRepository.increaseDebtBurden( "user1", "user1", 50, "asdasd"),
                "increaseDebtBurden() should throw when debtor and recipient are equal");
    }

    @Test
    public void testIncreaseDebtBurdenAddsDebtIfItDoesNotExist() {
        PersonalDebtsRepository personalDebtsRepository = new DefaultPersonalDebtsRepository(dependencyContainer);

        personalDebtsRepository.increaseDebtBurden( "user1", "user2", 50, "test reason");

        assertTrue(personalDebtsRepository.getDebtsOf("user1")
                .stream().anyMatch(d -> d.recipient().equals(user2) && d.amount() == 50 && d.reason().equals("test reason")),
                "Debt should be added if it didn't exist before.");
    }

    @Test
    public void testIncreaseDebtBurdenIncreasesDebtAmount() {
        PersonalDebtsRepository personalDebtsRepository = new DefaultPersonalDebtsRepository(dependencyContainer);;
        personalDebtsRepository.increaseDebtBurden("user1", "user2", 100, "test reason");

        personalDebtsRepository.increaseDebtBurden("user1", "user2", 30, "test reason");

        Optional<PersonalDebt> debt = personalDebtsRepository.getDebtsOf("user1")
                .stream().filter(d -> d.recipient().equals(user2) && d.reason().equals("test reason"))
                .findFirst();
        assertTrue(debt.isPresent(),
                "The debt should still be present.");
        assertEquals(130, debt.get().amount(),
                "Debt's amount should get increased.");
    }
}
