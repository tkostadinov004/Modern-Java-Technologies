package bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.data.implementations.PersonalDebtsCsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.dependency.DependencyContainer;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.NotificationType;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.PersonalDebt;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.dto.PersonalDebtDTO;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.NotificationsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.PersonalDebtsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.NonExistentDebtException;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.NonExistentUserException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DefaultPersonalDebtsRepositoryTest {
    private static final DependencyContainer  DEPENDENCY_CONTAINER = mock();
    private static final User USER_1 = new User("user1", "asd", "Test", "Test1");
    private static final User USER_2 = new User("user2", "asd", "Test", "Test1");
    private static final User USER_3 = new User("user3", "asd", "Test", "Test1");
    
    @BeforeAll
    public static void setUp() {
        NotificationsRepository notificationsRepository = mock();
        when( DEPENDENCY_CONTAINER.get(NotificationsRepository.class))
                .thenReturn(notificationsRepository);

        PersonalDebtsCsvProcessor csvProcessor = mock();
        when(csvProcessor.readAll())
                .thenReturn(Set.of());
        doAnswer((Answer<Void>) _ -> null)
                .when(csvProcessor).writeToFile(any());
        when( DEPENDENCY_CONTAINER.get(PersonalDebtsCsvProcessor.class))
                .thenReturn(csvProcessor);

        UserRepository userRepository = mock();
        when(userRepository.getUserByUsername("user1")).thenReturn(Optional.of(USER_1));
        when(userRepository.getUserByUsername("user2")).thenReturn(Optional.of(USER_2));
        when(userRepository.getUserByUsername("user3")).thenReturn(Optional.of(USER_3));
        when(userRepository.containsUser("user1")).thenReturn(true);
        when(userRepository.containsUser("user2")).thenReturn(true);
        when(userRepository.containsUser("user3")).thenReturn(true);
        when( DEPENDENCY_CONTAINER.get(UserRepository.class))
                .thenReturn(userRepository);
    }

    @Test
    public void testGetDebtsOfThrowsOnInvalidUsername() {
        PersonalDebtsRepository personalDebtsRepository = new DefaultPersonalDebtsRepository( DEPENDENCY_CONTAINER);

        assertThrows(IllegalArgumentException.class, () -> personalDebtsRepository.getDebtsOf(null),
                "getDebtsOf() should throw on null username");
        assertThrows(IllegalArgumentException.class, () -> personalDebtsRepository.getDebtsOf(""),
                "getDebtsOf() should throw on empty username");
        assertThrows(IllegalArgumentException.class, () -> personalDebtsRepository.getDebtsOf("   "),
                "getDebtsOf() should throw on blank username");
    }

    @Test
    public void testGetDebtsOfThrowsOnNonexistentUser() {
        PersonalDebtsRepository personalDebtsRepository = new DefaultPersonalDebtsRepository( DEPENDENCY_CONTAINER);;

        assertThrows(NonExistentUserException.class, () -> personalDebtsRepository.getDebtsOf( "asdasdasd"),
                "getExpensesOf() should throw on non existing user");
    }

    @Test
    public void testGetDebtsOfReturnsDebtsCorrectly() {
        PersonalDebtsRepository personalDebtsRepository = new DefaultPersonalDebtsRepository( DEPENDENCY_CONTAINER);
        personalDebtsRepository.increaseDebtBurden("user1", "user2", 100, "test reason");
        personalDebtsRepository.increaseDebtBurden("user2", "user1", 500, "another test");

        Set<PersonalDebt> expected = Set.of(new PersonalDebt(USER_1, USER_2, 100, "test reason"),
                new PersonalDebt(USER_2, USER_1, 500, "another test"));
        Set<PersonalDebt> actual = personalDebtsRepository.getDebtsOf("user1");

        assertTrue(expected.size() == actual.size() && expected.containsAll(actual) && actual.containsAll(expected),
                "getDebtsOf() should return all personal debts in which the user is a debtor or a recipient.");
    }

    @Test
    public void testLowerDebtBurdenThrowsOnInvalidDebtorUsername() {
        PersonalDebtsRepository personalDebtsRepository = new DefaultPersonalDebtsRepository( DEPENDENCY_CONTAINER);

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
        PersonalDebtsRepository personalDebtsRepository = new DefaultPersonalDebtsRepository( DEPENDENCY_CONTAINER);

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
        PersonalDebtsRepository personalDebtsRepository = new DefaultPersonalDebtsRepository( DEPENDENCY_CONTAINER);

        assertThrows(IllegalArgumentException.class,
                () -> personalDebtsRepository.lowerDebtBurden("asd", "asdd" , -1, "asd"),
                "lowerDebtBurden() should throw on null debtor username");
    }

    @Test
    public void testLowerDebtBurdenThrowsOnZeroAmount() {
        PersonalDebtsRepository personalDebtsRepository = new DefaultPersonalDebtsRepository( DEPENDENCY_CONTAINER);

        assertThrows(IllegalArgumentException.class,
                () -> personalDebtsRepository.lowerDebtBurden("asd", "asdd" , 0, "asd"),
                "lowerDebtBurden() should throw on null debtor username");
    }

    @Test
    public void testLowerDebtBurdenThrowsOnInvalidReason() {
        PersonalDebtsRepository personalDebtsRepository = new DefaultPersonalDebtsRepository( DEPENDENCY_CONTAINER);

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
        PersonalDebtsRepository personalDebtsRepository = new DefaultPersonalDebtsRepository( DEPENDENCY_CONTAINER);;

        assertThrows(NonExistentUserException.class, () -> personalDebtsRepository.lowerDebtBurden( "asdasdasd", "user1", 50, "asdasd"),
                "lowerDebtBurden() should throw on nonexisting debtor");
    }

    @Test
    public void testLowerDebtBurdenThrowsOnNonexistentRecipient() {
        PersonalDebtsRepository personalDebtsRepository = new DefaultPersonalDebtsRepository( DEPENDENCY_CONTAINER);;

        assertThrows(NonExistentUserException.class, () -> personalDebtsRepository.lowerDebtBurden( "user1", "asdasdasd", 50, "asdasd"),
                "lowerDebtBurden() should throw on nonexisting recipient");
    }

    @Test
    public void testLowerDebtBurdenThrowsOnEqualDebtorAndRecipient() {
        PersonalDebtsRepository personalDebtsRepository = new DefaultPersonalDebtsRepository( DEPENDENCY_CONTAINER);;

        assertThrows(IllegalArgumentException.class, () -> personalDebtsRepository.lowerDebtBurden( "user1", "user1", 50, "asdasd"),
                "lowerDebtBurden() should throw when debtor and recipient are equal");
    }

    @Test
    public void testLowerDebtBurdenThrowsOnNonexistentDebt() {
        PersonalDebtsRepository personalDebtsRepository = new DefaultPersonalDebtsRepository( DEPENDENCY_CONTAINER);

        assertThrows(NonExistentDebtException.class, () -> personalDebtsRepository.lowerDebtBurden( "user1", "user2", 50, "asdasd"),
                "lowerDebtBurden() should throw when debt doesn't exist");
    }

    @Test
    public void testLowerDebtBurdenRemovesDebtIfCompletelyPaidOff() {
        PersonalDebtsCsvProcessor csvProcessor = mock();
        when(csvProcessor.readAll())
                .thenReturn(Set.of());
        doAnswer((Answer<Void>) _ -> null)
                .when(csvProcessor).writeToFile(any());
        when( DEPENDENCY_CONTAINER.get(PersonalDebtsCsvProcessor.class))
                .thenReturn(csvProcessor);

        PersonalDebtsRepository personalDebtsRepository = new DefaultPersonalDebtsRepository( DEPENDENCY_CONTAINER);;
        personalDebtsRepository.increaseDebtBurden("user1", "user2", 100, "test reason");
        PersonalDebtDTO debtDTO = new PersonalDebtDTO("user1", "user2",100, "test reason");
        doAnswer((Answer<Void>) _ -> null)
                .when(csvProcessor).remove(debtDTO);

        personalDebtsRepository.lowerDebtBurden("user1", "user2", 100, "test reason");
        verify(csvProcessor, times(1))
                .remove(debtDTO);

        Optional<PersonalDebt> debt = personalDebtsRepository.getDebtsOf("user1")
                        .stream().filter(d -> d.recipient().equals(USER_2) && d.reason().equals("test reason"))
                        .findFirst();
        assertTrue(debt.isEmpty(),
                "If a debt is completely paid off, it should be deleted from the repository.");
    }

    @Test
    public void testLowerDebtBurdenLowersDebtAmount() {
        PersonalDebtsCsvProcessor csvProcessor = mock();
        when(csvProcessor.readAll())
                .thenReturn(Set.of());
        doAnswer((Answer<Void>) _ -> null)
                .when(csvProcessor).writeToFile(any());
        when( DEPENDENCY_CONTAINER.get(PersonalDebtsCsvProcessor.class))
                .thenReturn(csvProcessor);

        PersonalDebtDTO crudDTO = new PersonalDebtDTO("user1", "user2", 100, "test reason");
        PersonalDebtDTO modifiedDTO = new PersonalDebtDTO("user1", "user2", 70, "test reason");
        doAnswer((Answer<Void>) _ -> null)
                .when(csvProcessor).modify(crudDTO, modifiedDTO);

        PersonalDebtsRepository personalDebtsRepository = new DefaultPersonalDebtsRepository( DEPENDENCY_CONTAINER);;
        personalDebtsRepository.increaseDebtBurden("user1", "user2", 100, "test reason");

        personalDebtsRepository.lowerDebtBurden("user1", "user2", 30, "test reason");
        verify(csvProcessor, times(1))
                .modify(crudDTO, modifiedDTO);

        Optional<PersonalDebt> debt = personalDebtsRepository.getDebtsOf("user1")
                        .stream().filter(d -> d.recipient().equals(USER_2) && d.reason().equals("test reason"))
                        .findFirst();
        assertTrue(debt.isPresent(),
                "Even though its amount got lowered, the debt should still be present.");
        assertEquals(70, debt.get().amount(),
                "Debt's amount should get lowered.");
    }

    @Test
    public void testLowerDebtBurdenSendsNotification() {
        NotificationsRepository notificationsRepository = mock();
        when( DEPENDENCY_CONTAINER.get(NotificationsRepository.class))
                .thenReturn(notificationsRepository);

        PersonalDebtsRepository personalDebtsRepository = new DefaultPersonalDebtsRepository( DEPENDENCY_CONTAINER);;
        personalDebtsRepository.increaseDebtBurden("user1", "user2", 100, "test reason");

        personalDebtsRepository.lowerDebtBurden("user1", "user2", 30, "test reason");

        verify(notificationsRepository, times(1))
                .addNotificationForUser("user1",
                        "user2 approved your payment of 30.0 LV for test reason. You now owe them 70.0 LV.",
                        NotificationType.PERSONAL);
    }

    @Test
    public void testIncreaseDebtBurdenThrowsOnInvalidDebtorUsername() {
        PersonalDebtsRepository personalDebtsRepository = new DefaultPersonalDebtsRepository( DEPENDENCY_CONTAINER);

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
        PersonalDebtsRepository personalDebtsRepository = new DefaultPersonalDebtsRepository( DEPENDENCY_CONTAINER);

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
        PersonalDebtsRepository personalDebtsRepository = new DefaultPersonalDebtsRepository( DEPENDENCY_CONTAINER);

        assertThrows(IllegalArgumentException.class,
                () -> personalDebtsRepository.increaseDebtBurden("asd", "asdd" , -1, "asd"),
                "increaseDebtBurden() should throw on null debtor username");
    }

    @Test
    public void testIncreaseDebtBurdenThrowsOnZeroAmount() {
        PersonalDebtsRepository personalDebtsRepository = new DefaultPersonalDebtsRepository( DEPENDENCY_CONTAINER);

        assertThrows(IllegalArgumentException.class,
                () -> personalDebtsRepository.increaseDebtBurden("asd", "asdd" , 0, "asd"),
                "increaseDebtBurden() should throw on null debtor username");
    }

    @Test
    public void tesIncreaseDebtBurdenThrowsOnInvalidReason() {
        PersonalDebtsRepository personalDebtsRepository = new DefaultPersonalDebtsRepository( DEPENDENCY_CONTAINER);

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
        PersonalDebtsRepository personalDebtsRepository = new DefaultPersonalDebtsRepository( DEPENDENCY_CONTAINER);;

        assertThrows(NonExistentUserException.class, () -> personalDebtsRepository.increaseDebtBurden( "asdasdasd", "user1", 50, "asdasd"),
                "increaseDebtBurden() should throw on nonexisting debtor");
    }

    @Test
    public void testIncreaseDebtBurdenThrowsOnNonexistentRecipient() {
        PersonalDebtsRepository personalDebtsRepository = new DefaultPersonalDebtsRepository( DEPENDENCY_CONTAINER);;

        assertThrows(NonExistentUserException.class, () -> personalDebtsRepository.increaseDebtBurden( "user1", "asdasdasd", 50, "asdasd"),
                "increaseDebtBurden() should throw on nonexisting recipient");
    }

    @Test
    public void testIncreaseDebtBurdenThrowsOnEqualDebtorAndRecipient() {
        PersonalDebtsRepository personalDebtsRepository = new DefaultPersonalDebtsRepository( DEPENDENCY_CONTAINER);;

        assertThrows(IllegalArgumentException.class, () -> personalDebtsRepository.increaseDebtBurden( "user1", "user1", 50, "asdasd"),
                "increaseDebtBurden() should throw when debtor and recipient are equal");
    }

    @Test
    public void testIncreaseDebtBurdenAddsDebtIfItDoesNotExist() {
        PersonalDebtsCsvProcessor csvProcessor = mock();
        when(csvProcessor.readAll())
                .thenReturn(Set.of());
        doAnswer((Answer<Void>) _ -> null)
                .when(csvProcessor).writeToFile(any());
        when( DEPENDENCY_CONTAINER.get(PersonalDebtsCsvProcessor.class))
                .thenReturn(csvProcessor);

        PersonalDebtsRepository personalDebtsRepository = new DefaultPersonalDebtsRepository( DEPENDENCY_CONTAINER);

        personalDebtsRepository.increaseDebtBurden( "user1", "user2", 50, "test reason");
        verify(csvProcessor, times(0))
                .modify(any(), any());
        verify(csvProcessor, times(1))
                .writeToFile(any());

        assertTrue(personalDebtsRepository.getDebtsOf("user1")
                .stream().anyMatch(d -> d.recipient().equals(USER_2) && d.amount() == 50 && d.reason().equals("test reason")),
                "Debt should be added if it didn't exist before.");
    }

    @Test
    public void testIncreaseDebtBurdenIncreasesDebtAmount() {
        PersonalDebtsCsvProcessor csvProcessor = mock();
        when(csvProcessor.readAll())
                .thenReturn(Set.of());
        doAnswer((Answer<Void>) _ -> null)
                .when(csvProcessor).writeToFile(any());
        when( DEPENDENCY_CONTAINER.get(PersonalDebtsCsvProcessor.class))
                .thenReturn(csvProcessor);

        PersonalDebtDTO crudDTO = new PersonalDebtDTO("user1", "user2", 100, "test reason");
        PersonalDebtDTO modifiedDTO = new PersonalDebtDTO("user1", "user2", 130, "test reason");
        doAnswer((Answer<Void>) _ -> null)
                .when(csvProcessor).modify(crudDTO, modifiedDTO);

        PersonalDebtsRepository personalDebtsRepository = new DefaultPersonalDebtsRepository( DEPENDENCY_CONTAINER);;
        personalDebtsRepository.increaseDebtBurden("user1", "user2", 100, "test reason");
        personalDebtsRepository.increaseDebtBurden("user1", "user2", 30, "test reason");
        verify(csvProcessor, times(1))
                .modify(crudDTO, modifiedDTO);

        Optional<PersonalDebt> debt = personalDebtsRepository.getDebtsOf("user1")
                .stream().filter(d -> d.recipient().equals(USER_2) && d.reason().equals("test reason"))
                .findFirst();
        assertTrue(debt.isPresent(),
                "The debt should still be present.");
        assertEquals(130, debt.get().amount(),
                "Debt's amount should get increased.");
    }
}
