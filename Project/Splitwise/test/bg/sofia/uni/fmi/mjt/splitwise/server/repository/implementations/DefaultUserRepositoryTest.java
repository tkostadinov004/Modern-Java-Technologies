package bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.data.implementations.UserCsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.dependency.DependencyContainer;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.AlreadyRegisteredException;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.NonExistentUserException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultUserRepositoryTest {
    private static final DependencyContainer DEPENDENCY_CONTAINER = mock();

    @BeforeAll
    public static void setUp() {
        UserCsvProcessor csvProcessor = mock();
        User user = new User("user1", "asd", "Test", "Test1");
        when(csvProcessor.readAll())
                .thenReturn(Set.of(user));
        doAnswer((Answer<Void>) _ -> null).when(csvProcessor).writeToFile(user);
        when(DEPENDENCY_CONTAINER.get(UserCsvProcessor.class))
                .thenReturn(csvProcessor);
    }

    @Test
    public void testImportsFromCSVSuccessfully() {
        UserRepository userRepository = new DefaultUserRepository(DEPENDENCY_CONTAINER);
        Set<User> actual = userRepository.getAllUsers();
        Set<User> expected = Set.of(new User("user1", "asd", "Test", "Test1"));

        assertTrue(actual.size() == expected.size() && actual.containsAll(expected)
        && expected.containsAll(actual), "Upon creation of the repository, all users should be imported from the database");
    }

    @Test
    public void testContainsThrowsOnInvalidName() {
        UserRepository userRepository = new DefaultUserRepository(DEPENDENCY_CONTAINER);

        assertThrows(IllegalArgumentException.class, () -> userRepository.containsUser(null),
                "containsUser() should throw on null username");
        assertThrows(IllegalArgumentException.class, () -> userRepository.containsUser(""),
                "containsUser() should throw on empty username");
        assertThrows(IllegalArgumentException.class, () -> userRepository.containsUser("   "),
                "containsUser() should throw on blank username");
    }

    @Test
    public void testContainsReturnTrueIfUserIsPresent() {
        UserRepository userRepository = new DefaultUserRepository(DEPENDENCY_CONTAINER);

        assertTrue(userRepository.containsUser("user1"),
                "User should be present in the repository.");
    }

    @Test
    public void testContainsReturnsFalseIfUserIsNotPresent() {
        UserRepository userRepository = new DefaultUserRepository(DEPENDENCY_CONTAINER);

        assertFalse(userRepository.containsUser("user657tj"),
                "User should not be present in the repository.");
    }

    @Test
    public void testGetUserThrowsOnInvalidName() {
        UserRepository userRepository = new DefaultUserRepository(DEPENDENCY_CONTAINER);

        assertThrows(IllegalArgumentException.class, () -> userRepository.getUserByUsername(null),
                "getUserByUsername() should throw on null username");
        assertThrows(IllegalArgumentException.class, () -> userRepository.getUserByUsername(""),
                "getUserByUsername() should throw on empty username");
        assertThrows(IllegalArgumentException.class, () -> userRepository.getUserByUsername("   "),
                "getUserByUsername() should throw on blank username");
    }

    @Test
    public void testGetUserCorrectly() {
        UserRepository userRepository = new DefaultUserRepository(DEPENDENCY_CONTAINER);
        User expected = new User("user1", "asd", "Test", "Test1");
        Optional<User> actual = userRepository.getUserByUsername(expected.username());

        assertTrue(actual.isPresent(),
                "User should be present in the repository.");
        assertEquals(expected, actual.get(), "User should be present in the repository.");
    }

    @Test
    public void testGetSocketThrowsOnInvalidName() {
        UserRepository userRepository = new DefaultUserRepository(DEPENDENCY_CONTAINER);

        assertThrows(IllegalArgumentException.class, () -> userRepository.getSocketByUsername(null),
                "getUserByUsername() should throw on null username");
        assertThrows(IllegalArgumentException.class, () -> userRepository.getSocketByUsername(""),
                "getUserByUsername() should throw on empty username");
        assertThrows(IllegalArgumentException.class, () -> userRepository.getSocketByUsername("   "),
                "getUserByUsername() should throw on blank username");
    }

    @Test
    public void testGetSocketReturnsEmptyOnNonexistentUser() {
        UserRepository userRepository = new DefaultUserRepository(DEPENDENCY_CONTAINER);
        Optional<Socket> result = userRepository.getSocketByUsername("example");
        assertTrue(result.isEmpty(),
                "No socket should be returned when user is not present in the repository.");
    }

    @Test
    public void testGetSocketReturnsSocketCorrectly() {
        UserRepository userRepository = new DefaultUserRepository(DEPENDENCY_CONTAINER);
        Socket socket = mock();
        when(socket.getInetAddress())
                .thenReturn(new InetSocketAddress("testhost", 12345).getAddress());
        userRepository.bindSocketToUser("user1", socket);

        Optional<Socket> result = userRepository.getSocketByUsername("user1");
        assertTrue(result.isPresent(),
                "Socket should be returned if user is currently logged in");
        assertEquals(result.get(), socket, "Sockets should be equal.");
    }

    @Test
    public void testBindSocketThrowsOnInvalidName() {
        UserRepository userRepository = new DefaultUserRepository(DEPENDENCY_CONTAINER);
        Socket socket = mock();
        when(socket.getInetAddress())
                .thenReturn(new InetSocketAddress("testhost", 12345).getAddress());

        assertThrows(IllegalArgumentException.class, () -> userRepository.bindSocketToUser(null, socket),
                "bindSocketToUser() should throw on null username");
        assertThrows(IllegalArgumentException.class, () -> userRepository.bindSocketToUser("", socket),
                "bindSocketToUser() should throw on empty username");
        assertThrows(IllegalArgumentException.class, () -> userRepository.bindSocketToUser("   ", socket),
                "bindSocketToUser() should throw on blank username");
    }

    @Test
    public void testBindSocketThrowsOnNonexistentUser() {
        UserRepository userRepository = new DefaultUserRepository(DEPENDENCY_CONTAINER);
        Socket socket = mock();
        when(socket.getInetAddress())
                .thenReturn(new InetSocketAddress("testhost", 12345).getAddress());

        assertThrows(NonExistentUserException.class, () -> userRepository.bindSocketToUser("asdiuasd", socket),
                "bindSocketToUser() should throw on nonexistent user");
    }

    @Test
    public void testBindSocketThrowsOnNullSocket() {
        UserRepository userRepository = new DefaultUserRepository(DEPENDENCY_CONTAINER);

        assertThrows(IllegalArgumentException.class, () -> userRepository.bindSocketToUser("user1", null),
                "bindSocketToUser() should throw on null socket");
    }

    @Test
    public void testRegisterUserThrowsOnInvalidUsername() {
        UserRepository userRepository = new DefaultUserRepository(DEPENDENCY_CONTAINER);

        assertThrows(IllegalArgumentException.class, () -> userRepository.registerUser(null, "pass", "name", "name"),
                "registerUser() should throw on null username");
        assertThrows(IllegalArgumentException.class, () -> userRepository.registerUser("", "pass", "name", "name"),
                "registerUser() should throw on empty username");
        assertThrows(IllegalArgumentException.class, () -> userRepository.registerUser("   ", "pass", "name", "name"),
                "registerUser() should throw on blank username");
    }

    @Test
    public void testRegisterUserThrowsOnInvalidPassword() {
        UserRepository userRepository = new DefaultUserRepository(DEPENDENCY_CONTAINER);

        assertThrows(IllegalArgumentException.class, () -> userRepository.registerUser("user", null, "name", "name"),
                "registerUser() should throw on null password");
        assertThrows(IllegalArgumentException.class, () -> userRepository.registerUser("user", "", "name", "name"),
                "registerUser() should throw on empty password");
        assertThrows(IllegalArgumentException.class, () -> userRepository.registerUser("user", "   ", "name", "name"),
                "registerUser() should throw on blank password");
    }

    @Test
    public void testRegisterUserThrowsOnInvalidFirstName() {
        UserRepository userRepository = new DefaultUserRepository(DEPENDENCY_CONTAINER);

        assertThrows(IllegalArgumentException.class, () -> userRepository.registerUser("user", "pass", null, "name"),
                "registerUser() should throw on null first name");
        assertThrows(IllegalArgumentException.class, () -> userRepository.registerUser("user", "pass", "", "name"),
                "registerUser() should throw on empty first name");
        assertThrows(IllegalArgumentException.class, () -> userRepository.registerUser("user", "pass", "   ", "name"),
                "registerUser() should throw on blank first name");
    }

    @Test
    public void testRegisterUserThrowsOnInvalidLastName() {
        UserRepository userRepository = new DefaultUserRepository(DEPENDENCY_CONTAINER);

        assertThrows(IllegalArgumentException.class, () -> userRepository.registerUser("user", "pass", "name", null),
                "registerUser() should throw on null last name");
        assertThrows(IllegalArgumentException.class, () -> userRepository.registerUser("user", "pass", "name", ""),
                "registerUser() should throw on empty last name");
        assertThrows(IllegalArgumentException.class, () -> userRepository.registerUser("user", "pass", "name", "   "),
                "registerUser() should throw on blank last name");
    }

    @Test
    public void testRegisterUserThrowsOnAlreadyExistingUser() {
        UserRepository userRepository = new DefaultUserRepository(DEPENDENCY_CONTAINER);

        assertThrows(AlreadyRegisteredException.class, () -> userRepository.registerUser("user1", "pass", "name", "name"),
                "registerUser() should throw when trying to register a user with username that is already present in the repository");
    }

    @Test
    public void testRegisterUserAddsUserCorrectly() {
        UserRepository userRepository = new DefaultUserRepository(DEPENDENCY_CONTAINER);

        userRepository.registerUser("user2", "pass", "name", "name");
        assertTrue(userRepository.containsUser("user2"),
                "User should be present in the repository after registration.");
        assertNotEquals("pass", userRepository.getUserByUsername("user2").get().hashedPass(),
                "User's password should be stored in its hashed form in the repository.");
    }
}
