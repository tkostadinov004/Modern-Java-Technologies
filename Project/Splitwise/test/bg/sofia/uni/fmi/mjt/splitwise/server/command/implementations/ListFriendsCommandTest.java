package bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator.Authenticator;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.Command;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.exception.CommandArgumentsCountException;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.CommandHelp;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.ParameterContainer;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserFriendsRepository;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ListFriendsCommandTest {
    private static final PrintWriter PRINT_WRITER = mock();

    @Test
    public void testDoesNotWorkWithInsufficientArguments() {
        Authenticator authenticator = mock();
        UserFriendsRepository userFriendsRepository = mock();

        assertThrows(CommandArgumentsCountException.class,
                () -> new ListFriendsCommand(authenticator, userFriendsRepository, new String[2]),
                "Exception should be thrown if there are more arguments than needed");
    }

    @Test
    public void testDoesNotWorkWhenUserIsNotAuthenticated() {
        Authenticator authenticator = mock();
        when(authenticator.isAuthenticated()).thenReturn(false);

        UserFriendsRepository userFriendsRepository = mock();

        Command command =
                new ListFriendsCommand(authenticator, userFriendsRepository, new String[0]);
        assertFalse(command.execute(PRINT_WRITER),
                "Command should not work when user is not authenticated");

        verify(userFriendsRepository, times(0))
                .getFriendsOf(anyString());
    }

    @Test
    public void testWorksWhenUserIsAuthenticated() {
        Authenticator authenticator = mock();
        when(authenticator.getAuthenticatedUser())
                .thenReturn(new User("testuser", "pass", "fn", "ln"));
        when(authenticator.isAuthenticated()).thenReturn(true);

        UserFriendsRepository userFriendsRepository = mock();

        Command command =
                new ListFriendsCommand(authenticator, userFriendsRepository, new String[0]);

        assertTrue(command.execute(PRINT_WRITER),
                "Command should work when user is authenticated");

        verify(userFriendsRepository, times(1))
                .getFriendsOf("testuser");
    }

    @Test
    public void testHandlesErrors() {
        Authenticator authenticator = mock();
        when(authenticator.getAuthenticatedUser())
                .thenReturn(new User("testuser", "pass", "fn", "ln"));
        when(authenticator.isAuthenticated()).thenReturn(true);

        UserFriendsRepository userFriendsRepository = mock();
        doThrow(IllegalArgumentException.class)
                .when(userFriendsRepository)
                .getFriendsOf("testuser");
        Command command =
                new ListFriendsCommand(authenticator, userFriendsRepository, new String[0]);
        assertThrows(IllegalArgumentException.class, () -> userFriendsRepository.getFriendsOf("testuser"));
        assertFalse(command.execute(PRINT_WRITER),
                "An exception should be thrown if group already exists");
    }

    @Test
    public void testHelpReturnsCorrectly() {
        CommandHelp expected = new CommandHelp("list-friends",
                "lists all friends you have in your friend list",
                new ParameterContainer());

        CommandHelp actual = ListFriendsCommand.help();

        assertEquals(expected.toString(), actual.toString(),
                "Help method should correct information about the command.");
    }
}

