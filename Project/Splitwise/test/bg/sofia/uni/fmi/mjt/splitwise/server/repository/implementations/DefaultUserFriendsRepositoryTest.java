package bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.data.implementations.UserCsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.data.implementations.UserFriendsCsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.dependency.DependencyContainer;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.dto.FriendshipRelationDTO;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserFriendsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.AlreadyFriendsException;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.NonExistentUserException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultUserFriendsRepositoryTest {
    private static final DependencyContainer DEPENDENCY_CONTAINER = mock();

    @BeforeAll
    public static void setUp() {
        UserFriendsCsvProcessor friendsCsvProcessor = mock();
        when(friendsCsvProcessor.readAll())
                .thenReturn(Set.of(new FriendshipRelationDTO("user1", "user2"), new FriendshipRelationDTO("user2", "user1")));
        doAnswer((Answer<Void>) _ -> null)
                .when(friendsCsvProcessor).writeToFile(new FriendshipRelationDTO("user1", "user2"));
        doAnswer((Answer<Void>) _ -> null)
                .when(friendsCsvProcessor).writeToFile(new FriendshipRelationDTO("user1", "user3"));
        when(DEPENDENCY_CONTAINER.get(UserFriendsCsvProcessor.class))
                .thenReturn(friendsCsvProcessor);

        User user1 = new User("user1", "asd", "Test", "Test1");
        User user2 = new User("user2", "asd", "Test", "Test1");
        User user3 = new User("user3", "asd", "Test", "Test1");

        UserRepository userRepository = mock();
        when(userRepository.getUserByUsername("user1")).thenReturn(Optional.of(user1));
        when(userRepository.getUserByUsername("user2")).thenReturn(Optional.of(user2));
        when(userRepository.getUserByUsername("user3")).thenReturn(Optional.of(user3));
        when(userRepository.containsUser("user1")).thenReturn(true);
        when(userRepository.containsUser("user2")).thenReturn(true);
        when(userRepository.containsUser("user3")).thenReturn(true);
        when(DEPENDENCY_CONTAINER.get(UserRepository.class))
                .thenReturn(userRepository);
    }

    @Test
    public void testImportsFromCSVSuccessfully() {
        UserFriendsRepository friendsRepository = new DefaultUserFriendsRepository(DEPENDENCY_CONTAINER);

        assertTrue(friendsRepository.areFriends("user1", "user2"),
                "Friendship relation should be imported from the database upon creation of the repository.");
        assertTrue(friendsRepository.areFriends("user2", "user1"),
                "Friendship relation should be symmetric.");
    }

    @Test
    public void testAreFriendsThrowsOnInvalidFirstUsername() {
        UserFriendsRepository friendsRepository = new DefaultUserFriendsRepository(DEPENDENCY_CONTAINER);

        assertThrows(IllegalArgumentException.class, () -> friendsRepository.areFriends(null, "asdasd"),
                "areFriends() should throw on null username");
        assertThrows(IllegalArgumentException.class, () -> friendsRepository.areFriends("", "asdasd"),
                "areFriends() should throw on empty username");
        assertThrows(IllegalArgumentException.class, () -> friendsRepository.areFriends("   ", "asdasd"),
                "areFriends() should throw on blank username");
    }

    @Test
    public void testAreFriendsThrowsOnInvalidSecondUsername() {
        UserFriendsRepository friendsRepository = new DefaultUserFriendsRepository(DEPENDENCY_CONTAINER);

        assertThrows(IllegalArgumentException.class, () -> friendsRepository.areFriends("asdasd", null),
                "areFriends() should throw on null username");
        assertThrows(IllegalArgumentException.class, () -> friendsRepository.areFriends("asdasd", ""),
                "areFriends() should throw on empty username");
        assertThrows(IllegalArgumentException.class, () -> friendsRepository.areFriends("asdasd", "   "),
                "areFriends() should throw on blank username");
    }

    @Test
    public void testAreFriendsThrowsOnNonexistentFirstUser() {
        UserFriendsRepository friendsRepository = new DefaultUserFriendsRepository(DEPENDENCY_CONTAINER);

        assertThrows(NonExistentUserException.class, () -> friendsRepository.areFriends("aghsdhgasd", "user2"),
                "areFriends() should throw on non existing first user");
    }

    @Test
    public void testAreFriendsThrowsOnNonexistentSecondUser() {
        UserFriendsRepository friendsRepository = new DefaultUserFriendsRepository(DEPENDENCY_CONTAINER);

        assertThrows(NonExistentUserException.class, () -> friendsRepository.areFriends("user1", "asdasdasd"),
                "areFriends() should throw on non existing second user");
    }

    @Test
    public void testGetFriendsThrowsOnInvalidUsername() {
        UserFriendsRepository friendsRepository = new DefaultUserFriendsRepository(DEPENDENCY_CONTAINER);

        assertThrows(IllegalArgumentException.class, () -> friendsRepository.getFriendsOf( null),
                "getFriendsOf() should throw on null username");
        assertThrows(IllegalArgumentException.class, () -> friendsRepository.getFriendsOf(""),
                "getFriendsOf() should throw on empty username");
        assertThrows(IllegalArgumentException.class, () -> friendsRepository.getFriendsOf( "   "),
                "getFriendsOf() should throw on blank username");
    }

    @Test
    public void testGetFriendsThrowsOnNonexistentUser() {
        UserFriendsRepository friendsRepository = new DefaultUserFriendsRepository(DEPENDENCY_CONTAINER);

        assertThrows(NonExistentUserException.class, () -> friendsRepository.getFriendsOf( "asdasdasd"),
                "getFriendsOf() should throw on non existing second user");
    }

    @Test
    public void testGetFriendsReturnsEmptySetIfUserHasNoFriends() {
        UserFriendsRepository friendsRepository = new DefaultUserFriendsRepository(DEPENDENCY_CONTAINER);
        assertTrue(friendsRepository.getFriendsOf("user3").isEmpty(),
                "getFriendsOf() should return an empty set if a user has no friends");
    }

    @Test
    public void testGetFriendsReturnsCorrectly() {
        UserFriendsRepository friendsRepository = new DefaultUserFriendsRepository(DEPENDENCY_CONTAINER);
        Set<User> expected = Set.of(new User("user1", "asd", "Test", "Test1"));
        Set<User> actual = friendsRepository.getFriendsOf("user2");

        assertTrue(expected.size() == actual.size() &&
                expected.containsAll(actual) && actual.containsAll(expected),
                "All friends of a user should be present in the repository.");
    }

    @Test
    public void testMakeFriendsThrowsOnInvalidFirstUsername() {
        UserFriendsRepository friendsRepository = new DefaultUserFriendsRepository(DEPENDENCY_CONTAINER);

        assertThrows(IllegalArgumentException.class, () -> friendsRepository.makeFriends(null, "asdasd"),
                "makeFriends() should throw on null first username");
        assertThrows(IllegalArgumentException.class, () -> friendsRepository.makeFriends("", "asdasd"),
                "makeFriends() should throw on empty first username");
        assertThrows(IllegalArgumentException.class, () -> friendsRepository.makeFriends( "   ", "asdasd"),
                "makeFriends() should throw on blank first username");
    }

    @Test
    public void testMakeFriendsThrowsOnInvalidSecondUsername() {
        UserFriendsRepository friendsRepository = new DefaultUserFriendsRepository(DEPENDENCY_CONTAINER);

        assertThrows(IllegalArgumentException.class, () -> friendsRepository.makeFriends("asdasd", null),
                "makeFriends() should throw on null second username");
        assertThrows(IllegalArgumentException.class, () -> friendsRepository.makeFriends("asdasd", ""),
                "makeFriends() should throw on empty second username");
        assertThrows(IllegalArgumentException.class, () -> friendsRepository.makeFriends("asdasd", "   "),
                "makeFriends() should throw on blank second username");
    }

    @Test
    public void testMakeFriendsThrowsOnNonexistentFirstUser() {
        UserFriendsRepository friendsRepository = new DefaultUserFriendsRepository(DEPENDENCY_CONTAINER);

        assertThrows(NonExistentUserException.class, () -> friendsRepository.makeFriends("asdasdasd", "user2"),
                "makeFriends() should throw on non existing first user");
    }

    @Test
    public void testMakeFriendsThrowsOnNonexistentSecondUser() {
        UserFriendsRepository friendsRepository = new DefaultUserFriendsRepository(DEPENDENCY_CONTAINER);

        assertThrows(NonExistentUserException.class, () -> friendsRepository.makeFriends("user2", "asdasdasd"),
                "makeFriends() should throw on non existing second user");
    }

    @Test
    public void testMakeFriendsThrowsIfUserTriesToBefriendThemselves() {
        UserFriendsRepository friendsRepository = new DefaultUserFriendsRepository(DEPENDENCY_CONTAINER);

        assertThrows(IllegalArgumentException.class, () -> friendsRepository.makeFriends("user1", "user1"),
                "A user cannot befriend themselves.");
    }

    @Test
    public void testMakeFriendsThrowsIfUsersAreAlreadyFriends() {
        UserFriendsRepository friendsRepository = new DefaultUserFriendsRepository(DEPENDENCY_CONTAINER);

        assertThrows(AlreadyFriendsException.class, () -> friendsRepository.makeFriends("user1", "user2"),
                "makeFriends() should throw if 2 users are already friends.");
        assertThrows(AlreadyFriendsException.class, () -> friendsRepository.makeFriends("user2", "user1"),
                "makeFriends() should throw if 2 users are already friends.");
    }

    @Test
    public void testMakeFriendsWorksCorrectly() {
        UserFriendsRepository friendsRepository = new DefaultUserFriendsRepository(DEPENDENCY_CONTAINER);
        friendsRepository.makeFriends("user1", "user3");

        assertTrue(friendsRepository.areFriends("user1", "user3"),
                "Two users should be made friends if they are present in the repository and they are not already friends.");
        assertTrue(friendsRepository.areFriends("user3", "user1"),
                "Friendship relation should be symmetric.");
    }
}
