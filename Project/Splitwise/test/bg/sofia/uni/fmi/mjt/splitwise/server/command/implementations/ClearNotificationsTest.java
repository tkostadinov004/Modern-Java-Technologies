package bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator.Authenticator;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.Command;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.exception.CommandArgumentsCountException;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.CommandHelp;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.ParameterContainer;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.NotificationsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserFriendsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.AlreadyFriendsException;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.NonExistentUserException;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ClearNotificationsTest {
    private static final PrintWriter PRINT_WRITER = mock();

    @Test
    public void testDoesNotWorkWithInsufficientArguments() {
        Authenticator authenticator = mock();
        NotificationsRepository notificationsRepository = mock();

        assertThrows(CommandArgumentsCountException.class,
                () -> new ClearNotificationsCommand(authenticator, notificationsRepository, new String[1]),
                "Exception should be thrown if there are more arguments than needed");
    }

    @Test
    public void testDoesNotWorkWhenUserIsNotAuthenticated() {
        Authenticator authenticator = mock();
        when(authenticator.isAuthenticated()).thenReturn(false);

        NotificationsRepository notificationsRepository = mock();

        Command command =
                new ClearNotificationsCommand(authenticator, notificationsRepository, new String[0]);
        assertFalse(command.execute(PRINT_WRITER),
                "Command should not work when user is not authenticated");

        verify(notificationsRepository, times(0))
                .removeAllNotificationsForUser(anyString());
    }

    @Test
    public void testWorksWhenUserIsAuthenticated() {
        Authenticator authenticator = mock();
        when(authenticator.getAuthenticatedUser())
                .thenReturn(new User("testuser", "pass", "fn", "ln"));
        when(authenticator.isAuthenticated()).thenReturn(true);

        NotificationsRepository notificationsRepository = mock();

        Command command =
                new ClearNotificationsCommand(authenticator, notificationsRepository, new String[0]);

        assertTrue(command.execute(PRINT_WRITER),
                "Command should work when user is authenticated");

        verify(notificationsRepository, times(1))
                .removeAllNotificationsForUser("testuser");
    }

    @Test
    public void testHandlesInvalidUser() {
        Authenticator authenticator = mock();
        when(authenticator.getAuthenticatedUser())
                .thenReturn(new User("testuser", "pass", "fn", "ln"));
        when(authenticator.isAuthenticated()).thenReturn(true);

        NotificationsRepository notificationsRepository = mock();
        doThrow(NonExistentUserException.class)
                .when(notificationsRepository)
                .removeAllNotificationsForUser("testuser");
        Command command =
                new ClearNotificationsCommand(authenticator, notificationsRepository, new String[0]);
        assertThrows(NonExistentUserException.class, () -> notificationsRepository.removeAllNotificationsForUser(authenticator.getAuthenticatedUser().username()));
        assertFalse(command.execute(PRINT_WRITER),
                "An exception should be thrown if there is an invalid user");
    }

    @Test
    public void testHelpReturnsCorrectly() {
        CommandHelp expected = new CommandHelp("clear-notifications",
                "clears the notifications that you have, meaning that they won't be shown again when you log in",
                new ParameterContainer());

        CommandHelp actual = ClearNotificationsCommand.help();

        assertEquals(expected.toString(), actual.toString(),
                "Help method should correct information about the command.");
    }
}
