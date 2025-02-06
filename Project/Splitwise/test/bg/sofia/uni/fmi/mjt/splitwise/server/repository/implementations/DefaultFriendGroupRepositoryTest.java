package bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.data.implementations.FriendGroupsCsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.data.implementations.GroupDebtsCsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.dependency.DependencyContainer;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.FriendGroup;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.GroupDebt;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.dto.FriendGroupDTO;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.dto.GroupDebtDTO;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.FriendGroupRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.GroupDebtsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.NotificationsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.GroupAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.NonExistentUserException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import java.util.Map;
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

public class DefaultFriendGroupRepositoryTest {
    private static final DependencyContainer dependencyContainer = mock();
    private static final User user1 = new User("user1", "asd", "Test", "Test1");
    private static final User user2 = new User("user2", "asd", "Test", "Test1");
    private static final User user3 = new User("user3", "asd", "Test", "Test1");
    private static final User user4 = new User("user4", "asd", "Test", "Test1");

    @BeforeAll
    public static void setUp() {
        NotificationsRepository notificationsRepository = mock();
        when(dependencyContainer.get(NotificationsRepository.class))
                .thenReturn(notificationsRepository);

        FriendGroupsCsvProcessor csvProcessor = mock();
        when(csvProcessor.readAll())
                .thenReturn(Set.of());
        doAnswer((Answer<Void>) _ -> null)
                .when(csvProcessor).writeToFile(any());
        when(dependencyContainer.get(FriendGroupsCsvProcessor.class))
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
    }

    @Test
    public void testGetGroupsOfThrowsOnInvalidUsername() {
        FriendGroupRepository friendGroupRepository = new DefaultFriendGroupRepository(dependencyContainer);

        assertThrows(IllegalArgumentException.class, () -> friendGroupRepository.getGroupsOf(null),
                "getGroupsOf() should throw on null username");
        assertThrows(IllegalArgumentException.class, () -> friendGroupRepository.getGroupsOf(""),
                "getGroupsOf() should throw on empty username");
        assertThrows(IllegalArgumentException.class, () -> friendGroupRepository.getGroupsOf("   "),
                "getGroupsOf() should throw on blank username");
    }

    @Test
    public void testGetGroupsOfThrowsOnNonexistentUser() {
        FriendGroupRepository friendGroupRepository = new DefaultFriendGroupRepository(dependencyContainer);

        assertThrows(NonExistentUserException.class, () -> friendGroupRepository.getGroupsOf( "asdasdasd"),
                "getGroupsOf() should throw on non existing user");
    }

    @Test
    public void testGetGroupsOfReturnsGroupsCorrectly() {
        FriendGroupRepository friendGroupRepository = new DefaultFriendGroupRepository(dependencyContainer);
        friendGroupRepository.createGroup("testGroup", Set.of(user1.username(), user2.username(), user3.username()));
        friendGroupRepository.createGroup("testGroup1", Set.of(user1.username(), user2.username(), user4.username()));

        Set<FriendGroup> expected = Set.of(new FriendGroup("testGroup", Set.of(user1, user2, user3)),
                new FriendGroup("testGroup1", Set.of(user1, user2, user4)));
        Set<FriendGroup> actual = friendGroupRepository.getGroupsOf("user1");

        assertTrue(expected.size() == actual.size() &&
                        expected.containsAll(actual),
                "getGroupsOf() should return all groups in which the user is part of.");
    }

    @Test
    public void testGetGroupThrowsOnInvalidName() {
        FriendGroupRepository friendGroupRepository = new DefaultFriendGroupRepository(dependencyContainer);

        assertThrows(IllegalArgumentException.class, () -> friendGroupRepository.getGroup(null),
                "getGroup() should throw on null group name");
        assertThrows(IllegalArgumentException.class, () -> friendGroupRepository.getGroup(""),
                "getGroup() should throw on empty group name");
        assertThrows(IllegalArgumentException.class, () -> friendGroupRepository.getGroup("   "),
                "getGroup() should throw on blank group name");
    }

    @Test
    public void testGetGroupReturnsEmptyNonExistentGroup() {
        FriendGroupRepository friendGroupRepository = new DefaultFriendGroupRepository(dependencyContainer);

        Optional<FriendGroup> group = friendGroupRepository.getGroup("asdasda");
        assertTrue(group.isEmpty(),
                "An empty optional should be returned if a group is not present in the repository");
    }

    @Test
    public void testGetGroupReturnsGroupCorrectly() {
        FriendGroupRepository friendGroupRepository = new DefaultFriendGroupRepository(dependencyContainer);
        friendGroupRepository.createGroup("testGroup", Set.of(user1.username(), user2.username(), user3.username()));
        FriendGroup group = new FriendGroup("testGroup", Set.of(user1, user2, user3));

        Optional<FriendGroup> foundGroup = friendGroupRepository.getGroup("testGroup");
        assertTrue(foundGroup.isPresent(),
                "The group should be returned if it is present in the repository");
        assertEquals(group, foundGroup.get(),
                "The group should be returned if it is present in the repository");
    }

    @Test
    public void testContainsGroupByNameThrowsOnInvalidName() {
        FriendGroupRepository friendGroupRepository = new DefaultFriendGroupRepository(dependencyContainer);

        assertThrows(IllegalArgumentException.class, () -> friendGroupRepository.containsGroupByName(null),
                "containsGroupByName() should throw on null group name");
        assertThrows(IllegalArgumentException.class, () -> friendGroupRepository.containsGroupByName(""),
                "containsGroupByName() should throw on empty group name");
        assertThrows(IllegalArgumentException.class, () -> friendGroupRepository.containsGroupByName("   "),
                "containsGroupByName() should throw on blank group name");
    }

    @Test
    public void testContainsGroupByNameChecksCorrectly() {
        FriendGroupRepository friendGroupRepository = new DefaultFriendGroupRepository(dependencyContainer);
        friendGroupRepository.createGroup("testGroup", Set.of(user1.username(), user2.username(), user3.username()));

        assertTrue(friendGroupRepository.containsGroupByName("testGroup"),
                "containsGroupByName() should return true if the group is present in the repository");
        assertFalse(friendGroupRepository.containsGroupByName("asdasd"),
                "containsGroupByName() should return false if the group is not present in the repository");
    }

    @Test
    public void testCreateGroupThrowsOnInvalidGroupName() {
        FriendGroupRepository friendGroupRepository = new DefaultFriendGroupRepository(dependencyContainer);

        assertThrows(IllegalArgumentException.class, () -> friendGroupRepository.createGroup(null, Set.of(user1.username())),
                "createGroup() should throw on null group name");
        assertThrows(IllegalArgumentException.class, () -> friendGroupRepository.createGroup("", Set.of(user1.username())),
                "createGroup() should throw on empty group name");
        assertThrows(IllegalArgumentException.class, () -> friendGroupRepository.createGroup("   ", Set.of(user1.username())),
                "createGroup() should throw on blank group name");
    }

    @Test
    public void testCreateGroupThrowsOnNullParticipantsSet() {
        FriendGroupRepository friendGroupRepository = new DefaultFriendGroupRepository(dependencyContainer);

        assertThrows(IllegalArgumentException.class, () -> friendGroupRepository.createGroup("asdasd", null),
                "createGroup() should throw on null participants set");
    }

    @Test
    public void testCreateGroupThrowsOnEmptyParticipantsSet() {
        FriendGroupRepository friendGroupRepository = new DefaultFriendGroupRepository(dependencyContainer);

        assertThrows(IllegalArgumentException.class, () -> friendGroupRepository.createGroup("asdasd", Set.of()),
                "createGroup() should throw on empty participants set");
    }

    @Test
    public void testCreateGroupThrowsOnAlreadyExistingGroup() {
        FriendGroupRepository friendGroupRepository = new DefaultFriendGroupRepository(dependencyContainer);
        friendGroupRepository.createGroup("testGroup", Set.of(user1.username(), user2.username(), user3.username()));

        assertThrows(GroupAlreadyExistsException.class, () -> friendGroupRepository.createGroup("testGroup", Set.of(user1.username(), user2.username(), user4.username())),
                "createGroup() should throw on already existing group");
    }

    @Test
    public void testCreateGroupThrowsIfThereAreNonexistentMembers() {
        FriendGroupRepository friendGroupRepository = new DefaultFriendGroupRepository(dependencyContainer);

        assertThrows(NonExistentUserException.class, () -> friendGroupRepository.createGroup("testGroup", Set.of(user1.username(), "user2313")),
                "createGroup() should throw when there are nonexistent participants");
    }

    @Test
    public void testCreateGroupSuccessfully() {
        FriendGroupsCsvProcessor csvProcessor = mock();
        when(csvProcessor.readAll())
                .thenReturn(Set.of());
        doAnswer((Answer<Void>) _ -> null)
                .when(csvProcessor).writeToFile(any());
        when(dependencyContainer.get(FriendGroupsCsvProcessor.class))
                .thenReturn(csvProcessor);

        FriendGroupRepository friendGroupRepository = new DefaultFriendGroupRepository(dependencyContainer);
        FriendGroupDTO groupDTO = new FriendGroupDTO("testGroup", Set.of(user1.username(), user2.username(), user3.username()));
        friendGroupRepository.createGroup(groupDTO.name(), groupDTO.participantsUsernames());

        FriendGroup expected = new FriendGroup("testGroup", Set.of(user1, user2, user3));
        Optional<FriendGroup> actual = friendGroupRepository.getGroup(expected.name());
        assertTrue(actual.isPresent(),
                "A group should be present in the repository after being added");
        assertEquals(expected, actual.get(),
                "A group should be present in the repository after being added");
        verify(csvProcessor, times(1))
                .writeToFile(groupDTO);

    }
}
