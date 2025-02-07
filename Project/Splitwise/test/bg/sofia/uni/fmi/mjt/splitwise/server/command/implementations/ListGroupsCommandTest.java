package bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator.Authenticator;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.Command;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.exception.CommandArgumentsCountException;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.CommandHelp;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.ParameterContainer;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.FriendGroup;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.FriendGroupRepository;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.util.Set;

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

public class ListGroupsCommandTest {
    private static final PrintWriter printWriter = mock();

    @Test
    public void testDoesNotWorkWithInsufficientArguments() {
        Authenticator authenticator = mock();
        FriendGroupRepository friendGroupRepository = mock();

        assertThrows(CommandArgumentsCountException.class,
                () -> new ListGroupsCommand(authenticator, friendGroupRepository, new String[2]),
                "Exception should be thrown if there are more arguments than needed");
    }

    @Test
    public void testDoesNotWorkWhenUserIsNotAuthenticated() {
        Authenticator authenticator = mock();
        when(authenticator.isAuthenticated()).thenReturn(false);

        FriendGroupRepository friendGroupRepository = mock();

        Command command =
                new ListGroupsCommand(authenticator, friendGroupRepository, new String[0]);
        assertFalse(command.execute(printWriter),
                "Command should not work when user is not authenticated");

        verify(friendGroupRepository, times(0))
                .getGroupsOf(anyString());
    }

    @Test
    public void testWorksWhenUserIsAuthenticated() {
        Authenticator authenticator = mock();
        when(authenticator.getAuthenticatedUser())
                .thenReturn(new User("testuser", "pass", "fn", "ln"));
        when(authenticator.isAuthenticated()).thenReturn(true);

        FriendGroupRepository friendGroupRepository = mock();
        when(friendGroupRepository.getGroupsOf("testuser"))
                .thenReturn(Set.of(new FriendGroup("group", Set.of(new User("testuser", "pass", "fn", "ln")))));

        Command command =
                new ListGroupsCommand(authenticator, friendGroupRepository, new String[0]);

        assertTrue(command.execute(printWriter),
                "Command should work when user is authenticated");

        verify(friendGroupRepository, times(1))
                .getGroupsOf("testuser");
    }

    @Test
    public void testHandlesErrors() {
        Authenticator authenticator = mock();
        when(authenticator.getAuthenticatedUser())
                .thenReturn(new User("testuser", "pass", "fn", "ln"));
        when(authenticator.isAuthenticated()).thenReturn(true);

        FriendGroupRepository friendGroupRepository = mock();
        doThrow(IllegalArgumentException.class)
                .when(friendGroupRepository)
                .getGroupsOf("testuser");
        Command command =
                new ListGroupsCommand(authenticator, friendGroupRepository, new String[0]);
        assertThrows(IllegalArgumentException.class, () -> friendGroupRepository.getGroupsOf("testuser"));
        assertFalse(command.execute(printWriter),
                "An exception should be thrown if group already exists");
    }

    @Test
    public void testHelpReturnsCorrectly() {
        CommandHelp expected = new CommandHelp("list-groups",
                "lists all groups you are part of",
                new ParameterContainer());

        CommandHelp actual = ListGroupsCommand.help();

        assertEquals(expected.toString(), actual.toString(),
                "Help method should correct information about the command.");
    }
}

