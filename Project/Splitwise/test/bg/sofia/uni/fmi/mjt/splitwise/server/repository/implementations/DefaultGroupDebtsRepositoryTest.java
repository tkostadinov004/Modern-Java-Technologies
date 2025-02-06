package bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.data.implementations.GroupDebtsCsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.dependency.DependencyContainer;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.FriendGroup;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.NotificationType;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.GroupDebt;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.dto.GroupDebtDTO;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.FriendGroupRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.NotificationsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.GroupDebtsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.NonExistentDebtException;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.NonExistentUserException;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.NonExistingGroupException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import java.util.Map;
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

public class DefaultGroupDebtsRepositoryTest {
    private static final DependencyContainer dependencyContainer = mock();
    private static final User user1 = new User("user1", "asd", "Test", "Test1");
    private static final User user2 = new User("user2", "asd", "Test", "Test1");
    private static final User user3 = new User("user3", "asd", "Test", "Test1");
    private static final User user4 = new User("user4", "asd", "Test", "Test1");
    private static final FriendGroup group = new FriendGroup("testGroup", Set.of(user1, user2, user3));

    @BeforeAll
    public static void setUp() {
        NotificationsRepository notificationsRepository = mock();
        when(dependencyContainer.get(NotificationsRepository.class))
                .thenReturn(notificationsRepository);

        GroupDebtsCsvProcessor csvProcessor = mock();
        when(csvProcessor.readAll())
                .thenReturn(Set.of());
        doAnswer((Answer<Void>) _ -> null)
                .when(csvProcessor).writeToFile(any());
        when(dependencyContainer.get(GroupDebtsCsvProcessor.class))
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
    }

    @Test
    public void testGetDebtsOfThrowsOnInvalidUsername() {
        GroupDebtsRepository groupDebtsRepository = new DefaultGroupDebtsRepository(dependencyContainer);

        assertThrows(IllegalArgumentException.class, () -> groupDebtsRepository.getDebtsOf(null),
                "getDebtsOf() should throw on null username");
        assertThrows(IllegalArgumentException.class, () -> groupDebtsRepository.getDebtsOf(""),
                "getDebtsOf() should throw on empty username");
        assertThrows(IllegalArgumentException.class, () -> groupDebtsRepository.getDebtsOf("   "),
                "getDebtsOf() should throw on blank username");
    }

    @Test
    public void testGetDebtsOfThrowsOnNonexistentUser() {
        GroupDebtsRepository groupDebtsRepository = new DefaultGroupDebtsRepository(dependencyContainer);

        assertThrows(NonExistentUserException.class, () -> groupDebtsRepository.getDebtsOf( "asdasdasd"),
                "getExpensesOf() should throw on non existing user");
    }

    @Test
    public void testGetDebtsOfReturnsDebtsCorrectly() {
        GroupDebtsRepository groupDebtsRepository = new DefaultGroupDebtsRepository(dependencyContainer);
        groupDebtsRepository.increaseDebtBurden("user1", "user2", "testGroup", 100, "test reason");
        groupDebtsRepository.increaseDebtBurden("user2", "user1", "testGroup",500, "another test");

        Set<GroupDebt> expected = Set.of(new GroupDebt(user1, user2, group,100, "test reason"),
                new GroupDebt(user2, user1, group,500, "another test"));
        Map<FriendGroup, Set<GroupDebt>> actual = groupDebtsRepository.getDebtsOf("user1");

        assertEquals(1, actual.size(), "Only 1 group should be present.");
        assertTrue(expected.size() == actual.get(group).size() &&
                expected.containsAll(actual.get(group)),
                "getDebtsOf() should return all personal debts in a given group in which the user is a debtor or a recipient.");
    }

    @Test
    public void testLowerDebtBurdenThrowsOnInvalidDebtorUsername() {
        GroupDebtsRepository groupDebtsRepository = new DefaultGroupDebtsRepository(dependencyContainer);

        assertThrows(IllegalArgumentException.class,
                () -> groupDebtsRepository.lowerDebtBurden(null, "asd", "testGroup",50, "asd"),
                "lowerDebtBurden() should throw on null debtor username");
        assertThrows(IllegalArgumentException.class,
                () -> groupDebtsRepository.lowerDebtBurden("", "asd", "testGroup",50, "asd"),
                "lowerDebtBurden() should throw on empty debtor username");
        assertThrows(IllegalArgumentException.class,
                () -> groupDebtsRepository.lowerDebtBurden("   ", "asd", "testGroup",50, "asd"),
                "lowerDebtBurden() should throw on blank debtor username");
    }

    @Test
    public void testLowerDebtBurdenThrowsOnInvalidRecipientUsername() {
        GroupDebtsRepository groupDebtsRepository = new DefaultGroupDebtsRepository(dependencyContainer);

        assertThrows(IllegalArgumentException.class,
                () -> groupDebtsRepository.lowerDebtBurden("asd", null , "testGroup",50, "asd"),
                "lowerDebtBurden() should throw on null recipient username");
        assertThrows(IllegalArgumentException.class,
                () -> groupDebtsRepository.lowerDebtBurden("asd", "" ,"testGroup",50, "asd"),
                "lowerDebtBurden() should throw on empty recipient username");
        assertThrows(IllegalArgumentException.class,
                () -> groupDebtsRepository.lowerDebtBurden("asd", "   ", "testGroup",50, "asd"),
                "lowerDebtBurden() should throw on blank recipient username");
    }
    
    @Test
    public void testLowerDebtBurdenThrowsOnInvalidGroupName() {
        GroupDebtsRepository groupDebtsRepository = new DefaultGroupDebtsRepository(dependencyContainer);

        assertThrows(IllegalArgumentException.class,
                () -> groupDebtsRepository.lowerDebtBurden("user1", "user2" , null,50, "asd"),
                "lowerDebtBurden() should throw on null recipient username");
        assertThrows(IllegalArgumentException.class,
                () -> groupDebtsRepository.lowerDebtBurden("user1", "user2" ,"",50, "asd"),
                "lowerDebtBurden() should throw on empty recipient username");
        assertThrows(IllegalArgumentException.class,
                () -> groupDebtsRepository.lowerDebtBurden("user1", "user2", "   ",50, "asd"),
                "lowerDebtBurden() should throw on blank recipient username");
    }

    @Test
    public void testLowerDebtBurdenThrowsOnNegativeAmount() {
        GroupDebtsRepository groupDebtsRepository = new DefaultGroupDebtsRepository(dependencyContainer);

        assertThrows(IllegalArgumentException.class,
                () -> groupDebtsRepository.lowerDebtBurden("asd", "asdd" , "testGroup", -1, "asd"),
                "lowerDebtBurden() should throw on null debtor username");
    }

    @Test
    public void testLowerDebtBurdenThrowsOnZeroAmount() {
        GroupDebtsRepository groupDebtsRepository = new DefaultGroupDebtsRepository(dependencyContainer);

        assertThrows(IllegalArgumentException.class,
                () -> groupDebtsRepository.lowerDebtBurden("asd", "asdd" , "testGroup", 0, "asd"),
                "lowerDebtBurden() should throw on null debtor username");
    }

    @Test
    public void testLowerDebtBurdenThrowsOnInvalidReason() {
        GroupDebtsRepository groupDebtsRepository = new DefaultGroupDebtsRepository(dependencyContainer);

        assertThrows(IllegalArgumentException.class,
                () -> groupDebtsRepository.lowerDebtBurden("asd", "asdd" , "testGroup", 50, null),
                "lowerDebtBurden() should throw on null reason");
        assertThrows(IllegalArgumentException.class,
                () -> groupDebtsRepository.lowerDebtBurden("asd", "asdd" ,"testGroup", 50, ""),
                "lowerDebtBurden() should throw on empty reason");
        assertThrows(IllegalArgumentException.class,
                () -> groupDebtsRepository.lowerDebtBurden("asd", "asdd", "testGroup", 50, "   "),
                "lowerDebtBurden() should throw on blank reason");
    }

    @Test
    public void testLowerDebtBurdenThrowsOnNonexistentDebtor() {
        GroupDebtsRepository groupDebtsRepository = new DefaultGroupDebtsRepository(dependencyContainer);

        assertThrows(NonExistentUserException.class, () -> groupDebtsRepository.lowerDebtBurden( "asdasdasd", "user1", "testGroup", 50, "asdasd"),
                "lowerDebtBurden() should throw on nonexisting debtor");
    }

    @Test
    public void testLowerDebtBurdenThrowsOnNonexistentRecipient() {
        GroupDebtsRepository groupDebtsRepository = new DefaultGroupDebtsRepository(dependencyContainer);

        assertThrows(NonExistentUserException.class, () -> groupDebtsRepository.lowerDebtBurden( "user1", "asdasdasd", "testGroup", 50, "asdasd"),
                "lowerDebtBurden() should throw on nonexisting recipient");
    } 
    
    @Test
    public void testLowerDebtBurdenThrowsOnNonexistentGroup() {
        GroupDebtsRepository groupDebtsRepository = new DefaultGroupDebtsRepository(dependencyContainer);

        assertThrows(NonExistingGroupException.class, () -> groupDebtsRepository.lowerDebtBurden( "user1", "user2", "asdasdasdasd", 50, "asdasd"),
                "lowerDebtBurden() should throw on nonexisting group");
    }

    @Test
    public void testLowerDebtBurdenThrowsOnEqualDebtorAndRecipient() {
        GroupDebtsRepository groupDebtsRepository = new DefaultGroupDebtsRepository(dependencyContainer);

        assertThrows(IllegalArgumentException.class, () -> groupDebtsRepository.lowerDebtBurden( "user1", "user1", "testGroup", 50, "asdasd"),
                "lowerDebtBurden() should throw when debtor and recipient are equal");
    }

    @Test
    public void testLowerDebtBurdenThrowsOnNonexistentDebt() {
        GroupDebtsRepository groupDebtsRepository = new DefaultGroupDebtsRepository(dependencyContainer);

        assertThrows(NonExistentDebtException.class, () -> groupDebtsRepository.lowerDebtBurden( "user1", "user2", "testGroup", 50, "asdasd"),
                "lowerDebtBurden() should throw when debt doesn't exist");
    }

    @Test
    public void testLowerDebtBurdenRemovesDebtIfCompletelyPaidOff() {
        GroupDebtsCsvProcessor csvProcessor = mock();
        when(csvProcessor.readAll())
                .thenReturn(Set.of());
        doAnswer((Answer<Void>) _ -> null)
                .when(csvProcessor).writeToFile(any());
        when(dependencyContainer.get(GroupDebtsCsvProcessor.class))
                .thenReturn(csvProcessor);

        GroupDebtsRepository groupDebtsRepository = new DefaultGroupDebtsRepository(dependencyContainer);
        groupDebtsRepository.increaseDebtBurden("user1", "user2", "testGroup",100, "test reason");
        GroupDebtDTO debtDTO = new GroupDebtDTO("user1", "user2", "testGroup",100, "test reason");
        doAnswer((Answer<Void>) _ -> null)
                .when(csvProcessor).remove(debtDTO);

        groupDebtsRepository.lowerDebtBurden("user1", "user2", "testGroup",100, "test reason");

        verify(csvProcessor, times(1))
                .remove(debtDTO);

        Map<FriendGroup, Set<GroupDebt>> debtsMap = groupDebtsRepository.getDebtsOf("user1");
        assertTrue(debtsMap.containsKey(group),
                "Even though its amount got lowered, the debt should still be present.");

        Optional<GroupDebt> debt = debtsMap
                .get(group)
                .stream().filter(d -> d.recipient().equals(user2) && d.group().equals(group) && d.reason().equals("test reason"))
                .findFirst();
        assertTrue(debt.isEmpty(),
                "If a debt is completely paid off, it should be deleted from the repository.");
    }

    @Test
    public void testLowerDebtBurdenLowersDebtAmount() {
        GroupDebtsCsvProcessor csvProcessor = mock();
        when(csvProcessor.readAll())
                .thenReturn(Set.of());
        doAnswer((Answer<Void>) _ -> null)
                .when(csvProcessor).writeToFile(any());
        when(dependencyContainer.get(GroupDebtsCsvProcessor.class))
                .thenReturn(csvProcessor);
        GroupDebtDTO crudDTO = new GroupDebtDTO("user1", "user2", "testGroup",100, "test reason");
        GroupDebtDTO modifiedDTO = new GroupDebtDTO("user1", "user2", "testGroup",70, "test reason");
        doAnswer((Answer<Void>) _ -> null)
                .when(csvProcessor).modify(crudDTO, modifiedDTO);

        GroupDebtsRepository groupDebtsRepository = new DefaultGroupDebtsRepository(dependencyContainer);
        groupDebtsRepository.increaseDebtBurden("user1", "user2", "testGroup",100, "test reason");

        groupDebtsRepository.lowerDebtBurden("user1", "user2", "testGroup",30, "test reason");

        verify(csvProcessor, times(1))
                .modify(crudDTO, modifiedDTO);

        Map<FriendGroup, Set<GroupDebt>> debtsMap = groupDebtsRepository.getDebtsOf("user1");
        assertTrue(debtsMap.containsKey(group),
                "Even though its amount got lowered, the debt should still be present.");

        Optional<GroupDebt> debt = groupDebtsRepository.getDebtsOf("user1")
                .get(group)
                .stream().filter(d -> d.recipient().equals(user2) && d.group().equals(group) && d.reason().equals("test reason"))
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

        GroupDebtsRepository groupDebtsRepository = new DefaultGroupDebtsRepository(dependencyContainer);
        groupDebtsRepository.increaseDebtBurden("user1", "user2", "testGroup",100, "test reason");

        groupDebtsRepository.lowerDebtBurden("user1", "user2", "testGroup",30, "test reason");

        verify(notificationsRepository, times(1))
                .addNotificationForUser("user1",
                        "user2 approved your payment of 30.0 LV in group testGroup for test reason. You now owe them 70.0 LV.",
                        NotificationType.PERSONAL);
    }

    @Test
    public void testIncreaseDebtBurdenThrowsOnInvalidDebtorUsername() {
        GroupDebtsRepository groupDebtsRepository = new DefaultGroupDebtsRepository(dependencyContainer);

        assertThrows(IllegalArgumentException.class,
                () -> groupDebtsRepository.increaseDebtBurden(null, "asd", "testGroup",50, "asd"),
                "increaseDebtBurden() should throw on null debtor username");
        assertThrows(IllegalArgumentException.class,
                () -> groupDebtsRepository.increaseDebtBurden("", "asd", "testGroup",50, "asd"),
                "increaseDebtBurden() should throw on empty debtor username");
        assertThrows(IllegalArgumentException.class,
                () -> groupDebtsRepository.increaseDebtBurden("   ", "asd", "testGroup",50, "asd"),
                "increaseDebtBurden() should throw on blank debtor username");
    }

    @Test
    public void testIncreaseDebtBurdenThrowsOnInvalidRecipientUsername() {
        GroupDebtsRepository groupDebtsRepository = new DefaultGroupDebtsRepository(dependencyContainer);

        assertThrows(IllegalArgumentException.class,
                () -> groupDebtsRepository.increaseDebtBurden("asd", null , "testGroup",50, "asd"),
                "increaseDebtBurden() should throw on null recipient username");
        assertThrows(IllegalArgumentException.class,
                () -> groupDebtsRepository.increaseDebtBurden("asd", "" ,"testGroup",50, "asd"),
                "increaseDebtBurden() should throw on empty recipient username");
        assertThrows(IllegalArgumentException.class,
                () -> groupDebtsRepository.increaseDebtBurden("asd", "   ", "testGroup",50, "asd"),
                "increaseDebtBurden() should throw on blank recipient username");
    }

    @Test
    public void testIncreaseDebtBurdenThrowsOnInvalidGroupName() {
        GroupDebtsRepository groupDebtsRepository = new DefaultGroupDebtsRepository(dependencyContainer);

        assertThrows(IllegalArgumentException.class,
                () -> groupDebtsRepository.increaseDebtBurden("user1", "user2" , null,50, "asd"),
                "increaseDebtBurden() should throw on null recipient username");
        assertThrows(IllegalArgumentException.class,
                () -> groupDebtsRepository.increaseDebtBurden("user1", "user2" ,"",50, "asd"),
                "increaseDebtBurden() should throw on empty recipient username");
        assertThrows(IllegalArgumentException.class,
                () -> groupDebtsRepository.increaseDebtBurden("user1", "user2", "   ",50, "asd"),
                "increaseDebtBurden() should throw on blank recipient username");
    }

    @Test
    public void testIncreaseDebtBurdenThrowsOnNegativeAmount() {
        GroupDebtsRepository groupDebtsRepository = new DefaultGroupDebtsRepository(dependencyContainer);

        assertThrows(IllegalArgumentException.class,
                () -> groupDebtsRepository.increaseDebtBurden("asd", "asdd" , "testGroup",-1, "asd"),
                "increaseDebtBurden() should throw on null debtor username");
    }

    @Test
    public void testIncreaseDebtBurdenThrowsOnZeroAmount() {
        GroupDebtsRepository groupDebtsRepository = new DefaultGroupDebtsRepository(dependencyContainer);

        assertThrows(IllegalArgumentException.class,
                () -> groupDebtsRepository.increaseDebtBurden("asd", "asdd" , "testGroup",0, "asd"),
                "increaseDebtBurden() should throw on null debtor username");
    }

    @Test
    public void tesIncreaseDebtBurdenThrowsOnInvalidReason() {
        GroupDebtsRepository groupDebtsRepository = new DefaultGroupDebtsRepository(dependencyContainer);

        assertThrows(IllegalArgumentException.class,
                () -> groupDebtsRepository.increaseDebtBurden("asd", "asdd" ,"testGroup", 50, null),
                "increaseDebtBurden() should throw on null reason");
        assertThrows(IllegalArgumentException.class,
                () -> groupDebtsRepository.increaseDebtBurden("asd", "asdd" ,"testGroup",50, ""),
                "increaseDebtBurden() should throw on empty reason");
        assertThrows(IllegalArgumentException.class,
                () -> groupDebtsRepository.increaseDebtBurden("asd", "asdd", "testGroup",50, "   "),
                "increaseDebtBurden() should throw on blank reason");
    }

    @Test
    public void testIncreaseDebtBurdenThrowsOnNonexistentDebtor() {
        GroupDebtsRepository groupDebtsRepository = new DefaultGroupDebtsRepository(dependencyContainer);

        assertThrows(NonExistentUserException.class, () -> groupDebtsRepository.increaseDebtBurden( "asdasdasd", "user1", "testGroup",50, "asdasd"),
                "increaseDebtBurden() should throw on nonexisting debtor");
    }

    @Test
    public void testIncreaseDebtBurdenThrowsOnNonexistentRecipient() {
        GroupDebtsRepository groupDebtsRepository = new DefaultGroupDebtsRepository(dependencyContainer);

        assertThrows(NonExistentUserException.class, () -> groupDebtsRepository.increaseDebtBurden( "user1", "asdasdasd","testGroup", 50, "asdasd"),
                "increaseDebtBurden() should throw on nonexisting recipient");
    }

    @Test
    public void testIncreaseDebtBurdenThrowsOnNonexistentGroup() {
        GroupDebtsRepository groupDebtsRepository = new DefaultGroupDebtsRepository(dependencyContainer);

        assertThrows(NonExistingGroupException.class, () -> groupDebtsRepository.increaseDebtBurden( "user1", "user2","fdgdfg", 50, "asdasd"),
                "increaseDebtBurden() should throw on nonexisting group");
    }

    @Test
    public void testIncreaseDebtBurdenThrowsOnEqualDebtorAndRecipient() {
        GroupDebtsRepository groupDebtsRepository = new DefaultGroupDebtsRepository(dependencyContainer);

        assertThrows(IllegalArgumentException.class, () -> groupDebtsRepository.increaseDebtBurden( "user1", "user1", "testGroup",50, "asdasd"),
                "increaseDebtBurden() should throw when debtor and recipient are equal");
    }

    @Test
    public void testIncreaseDebtBurdenAddsDebtIfItDoesNotExist() {
        GroupDebtsCsvProcessor csvProcessor = mock();
        when(csvProcessor.readAll())
                .thenReturn(Set.of());
        doAnswer((Answer<Void>) _ -> null)
                .when(csvProcessor).writeToFile(any());
        when(dependencyContainer.get(GroupDebtsCsvProcessor.class))
                .thenReturn(csvProcessor);

        GroupDebtsRepository groupDebtsRepository = new DefaultGroupDebtsRepository(dependencyContainer);
        groupDebtsRepository.increaseDebtBurden( "user1", "user2", "testGroup",50, "test reason");
        verify(csvProcessor, times(0))
                .modify(any(), any());
        verify(csvProcessor, times(1))
                .writeToFile(any());

        Map<FriendGroup, Set<GroupDebt>> debtsMap = groupDebtsRepository.getDebtsOf("user1");
        assertTrue(debtsMap.containsKey(group),
                "Debt should be added if it didn't exist before.");
        assertTrue(groupDebtsRepository
                        .getDebtsOf("user1")
                        .get(group)
                        .stream()
                        .anyMatch(d -> d.recipient().equals(user2) && d.group().equals(group) && d.amount() == 50 && d.reason().equals("test reason")),
                "Debt should be added if it didn't exist before.");
    }

    @Test
    public void testIncreaseDebtBurdenIncreasesDebtAmount() {
        GroupDebtsCsvProcessor csvProcessor = mock();
        when(csvProcessor.readAll())
                .thenReturn(Set.of());
        doAnswer((Answer<Void>) _ -> null)
                .when(csvProcessor).writeToFile(any());
        when(dependencyContainer.get(GroupDebtsCsvProcessor.class))
                .thenReturn(csvProcessor);

        GroupDebtDTO crudDTO = new GroupDebtDTO("user1", "user2", "testGroup",100, "test reason");
        GroupDebtDTO modifiedDTO = new GroupDebtDTO("user1", "user2", "testGroup",130, "test reason");
        doAnswer((Answer<Void>) _ -> null)
                .when(csvProcessor).modify(crudDTO, modifiedDTO);

        GroupDebtsRepository groupDebtsRepository = new DefaultGroupDebtsRepository(dependencyContainer);
        groupDebtsRepository.increaseDebtBurden("user1", "user2", "testGroup", 100, "test reason");
        groupDebtsRepository.increaseDebtBurden("user1", "user2", "testGroup",30, "test reason");
        verify(csvProcessor, times(1))
                .modify(crudDTO, modifiedDTO);

        Optional<GroupDebt> debt = groupDebtsRepository
                .getDebtsOf("user1")
                .get(group)
                .stream()
                .filter(d -> d.recipient().equals(user2) && d.group().equals(group) && d.reason().equals("test reason"))
                .findFirst();
        assertTrue(debt.isPresent(),
                "The debt should still be present.");
        assertEquals(130, debt.get().amount(),
                "Debt's amount should get increased.");
    }
}
