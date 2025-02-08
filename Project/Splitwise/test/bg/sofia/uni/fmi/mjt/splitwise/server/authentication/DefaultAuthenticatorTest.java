package bg.sofia.uni.fmi.mjt.splitwise.server.authentication;

import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator.Authenticator;
import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator.DefaultAuthenticator;
import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.exception.AlreadyAuthenticatedException;
import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.exception.InvalidCredentialsException;
import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.exception.NotAuthenticatedException;
import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.hash.PasswordHasher;
import bg.sofia.uni.fmi.mjt.splitwise.server.data.implementations.UserFriendsCsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.dependency.DependencyContainer;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.dto.FriendshipRelationDTO;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserFriendsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.NonExistentUserException;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations.DefaultUserFriendsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations.DefaultUserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import java.net.Socket;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultAuthenticatorTest {
    private static final DependencyContainer  DEPENDENCY_CONTAINER = mock();
    private static final Socket SOCKET = mock();
    private static final User  USER_1 = new User(" USER_1", "asd", "Test", "Test1");

    @BeforeAll
    public static void setUp() {
        Logger logger = mock();
        when( DEPENDENCY_CONTAINER.get(Logger.class))
                .thenReturn(logger);

        User user2 = new User("user2", "fgh", "Test", "Test1");
        User user3 = new User("user3", "hjk", "Test", "Test1");

        UserRepository userRepository = mock();
        when(userRepository.getUserByUsername(" USER_1")).thenReturn(Optional.of( USER_1));
        when(userRepository.getUserByUsername("user2")).thenReturn(Optional.of(user2));
        when(userRepository.getUserByUsername("user3")).thenReturn(Optional.of(user3));
        when(userRepository.containsUser(" USER_1")).thenReturn(true);
        when(userRepository.containsUser("user2")).thenReturn(true);
        when(userRepository.containsUser("user3")).thenReturn(true);
        when( DEPENDENCY_CONTAINER.get(UserRepository.class))
                .thenReturn(userRepository);

        PasswordHasher hasher = mock();
        when(hasher.hash("pass1")).thenReturn("asd");
        when(hasher.hash("pass2")).thenReturn("fgh");
        when(hasher.hash("pass3")).thenReturn("hjk");
        when( DEPENDENCY_CONTAINER.get(PasswordHasher.class))
                .thenReturn(hasher);
    }

    @Test
    public void isAuthenticatedReturnsFalseIfUserIsNotAuthenticated() {
        Authenticator authenticator = new DefaultAuthenticator( DEPENDENCY_CONTAINER, SOCKET);
        assertFalse(authenticator.isAuthenticated(),
                "isAuthenticated() should return false if a user is not authenticated");
    }

    @Test
    public void testAuthenticateThrowsOnInvalidName() {
        Authenticator authenticator = new DefaultAuthenticator( DEPENDENCY_CONTAINER, SOCKET);

        assertThrows(IllegalArgumentException.class, () -> authenticator.authenticate(null, "pass"),
                "authenticate() should throw on null username");
        assertThrows(IllegalArgumentException.class, () -> authenticator.authenticate("", "pass"),
                "authenticate() should throw on empty username");
        assertThrows(IllegalArgumentException.class, () -> authenticator.authenticate("   ", "pass"),
                "authenticate() should throw on blank username");
    }

    @Test
    public void testAuthenticateThrowsOnInvalidPassword() {
        Authenticator authenticator = new DefaultAuthenticator( DEPENDENCY_CONTAINER, SOCKET);

        assertThrows(IllegalArgumentException.class, () -> authenticator.authenticate(" USER_1", null),
                "authenticate() should throw on null password");
        assertThrows(IllegalArgumentException.class, () -> authenticator.authenticate(" USER_1", ""),
                "authenticate() should throw on empty password");
        assertThrows(IllegalArgumentException.class, () -> authenticator.authenticate(" USER_1", "   "),
                "authenticate() should throw on blank password");
    }

    @Test
    public void testAuthenticateThrowsIfUserIsAlreadyAuthenticated() throws AlreadyAuthenticatedException {
        Authenticator authenticator = new DefaultAuthenticator( DEPENDENCY_CONTAINER, SOCKET);
        authenticator.authenticate(" USER_1", "pass1");

        assertThrows(AlreadyAuthenticatedException.class, () -> authenticator.authenticate("user2", "pass2"),
                "authenticate() should throw on null password");
    }

    @Test
    public void testAuthenticateThrowsOnNonexistentUser() {
        Authenticator authenticator = new DefaultAuthenticator( DEPENDENCY_CONTAINER, SOCKET);

        assertThrows(InvalidCredentialsException.class, () -> authenticator.authenticate("aghsdhgasd", "pass2"),
                "authenticate() should throw on non existing user");
    }

    @Test
    public void testAuthenticateThrowsOnWrongPassword() {
        Authenticator authenticator = new DefaultAuthenticator( DEPENDENCY_CONTAINER, SOCKET);

        assertThrows(InvalidCredentialsException.class, () -> authenticator.authenticate(" USER_1", "pass3"),
                "authenticate() should throw on wrong password");
    }

    @Test
    public void testAuthenticatesSuccessfully() throws AlreadyAuthenticatedException {
        Authenticator authenticator = new DefaultAuthenticator( DEPENDENCY_CONTAINER, SOCKET);
        authenticator.authenticate(" USER_1", "pass1");

        assertEquals( USER_1, authenticator.getAuthenticatedUser(),
                "User should be saved in the authentication token after authenticating");
    }

    @Test
    public void testLogoutThrowsWhenUserIsNotLoggedIn() {
        Authenticator authenticator = new DefaultAuthenticator( DEPENDENCY_CONTAINER, SOCKET);

        assertThrows(NotAuthenticatedException.class, () -> authenticator.logout(),
                "logout() should throw when a user is not authenticated");
    }

    @Test
    public void testLogsOutSuccessfully() throws AlreadyAuthenticatedException, NotAuthenticatedException {
        Authenticator authenticator = new DefaultAuthenticator( DEPENDENCY_CONTAINER, SOCKET);
        authenticator.authenticate(" USER_1", "pass1");
        authenticator.logout();

        assertEquals(null, authenticator.getAuthenticatedUser(),
                "User should be removed from the authentication token after logging out");
    }
}
