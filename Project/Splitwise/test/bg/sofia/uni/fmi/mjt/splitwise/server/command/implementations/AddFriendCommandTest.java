package bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator.Authenticator;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.Command;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.exception.CommandArgumentsCountException;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.CommandHelp;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.ParameterContainer;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserFriendsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.AlreadyFriendsException;
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

public class AddFriendCommandTest {
    private static final PrintWriter PRINT_WRITER = mock();

    @Test
    public void testDoesNotWorkWithInsufficientArguments() {
        Authenticator authenticator = mock();
        UserFriendsRepository userFriendsRepository = mock();

        assertThrows(CommandArgumentsCountException.class,
                () -> new AddFriendCommand(authenticator, userFriendsRepository, new String[0]),
                "Exception should be thrown if there are less arguments than needed");
        assertThrows(CommandArgumentsCountException.class,
                () -> new AddFriendCommand(authenticator, userFriendsRepository, new String[2]),
                "Exception should be thrown if there are more arguments than needed");
    }

    @Test
    public void testDoesNotWorkWhenUserIsNotAuthenticated() {
        Authenticator authenticator = mock();
        when(authenticator.isAuthenticated()).thenReturn(false);

        UserFriendsRepository userFriendsRepository = mock();

        Command command =
                new AddFriendCommand(authenticator, userFriendsRepository, new String[]{"user"});
        assertFalse(command.execute(PRINT_WRITER),
                "Command should not work when user is not authenticated");

        verify(userFriendsRepository, times(0))
                .makeFriends(anyString(), anyString());
    }

    @Test
    public void testWorksWhenUserIsAuthenticated() {
        Authenticator authenticator = mock();
        when(authenticator.getAuthenticatedUser())
                .thenReturn(new User("testuser", "pass", "fn", "ln"));
        when(authenticator.isAuthenticated()).thenReturn(true);

        UserFriendsRepository userFriendsRepository = mock();

        Command command =
                new AddFriendCommand(authenticator, userFriendsRepository, new String[]{"user"});

        assertTrue(command.execute(PRINT_WRITER),
                "Command should work when user is authenticated");

        verify(userFriendsRepository, times(1))
                .makeFriends("testuser", "user");
    }

    @Test
    public void testHandlesAlreadyFriends() {
        Authenticator authenticator = mock();
        when(authenticator.getAuthenticatedUser())
                .thenReturn(new User("testuser", "pass", "fn", "ln"));
        when(authenticator.isAuthenticated()).thenReturn(true);

        UserFriendsRepository userFriendsRepository = mock();
        doThrow(AlreadyFriendsException.class)
                .when(userFriendsRepository)
                .makeFriends("testuser", "user");
        Command command =
                new AddFriendCommand(authenticator, userFriendsRepository, new String[]{"user"});
        assertThrows(AlreadyFriendsException.class, () -> userFriendsRepository.makeFriends(authenticator.getAuthenticatedUser().username(), "user"));
        assertFalse(command.execute(PRINT_WRITER),
                "An exception should be thrown if 2 people are already friends");
    }

    @Test
    public void testHelpReturnsCorrectly() {
        ParameterContainer parameters = new ParameterContainer();
        parameters.addParameter("username", "the username of the person you want to add as a friend", false);
        CommandHelp expected = new CommandHelp("add-friend",
                "adds the specified user as your friend, allowing you to split bills and chat with them",
                parameters);

        CommandHelp actual = AddFriendCommand.help();

        assertEquals(expected.toString(), actual.toString(),
                "Help method should correct information about the command.");
    }
}
