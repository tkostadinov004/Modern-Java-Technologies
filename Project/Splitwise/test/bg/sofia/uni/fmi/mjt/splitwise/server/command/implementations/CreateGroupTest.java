package bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator.Authenticator;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.Command;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.exception.CommandArgumentsCountException;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.CommandHelp;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.ParameterContainer;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.FriendGroupRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.AlreadyFriendsException;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.GroupAlreadyExistsException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.io.PrintWriter;
import java.util.Set;

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

public class CreateGroupTest {
    private static final PrintWriter PRINT_WRITER = mock();

    @Test
    public void testDoesNotWorkWithInsufficientArguments() {
        Authenticator authenticator = mock();
        FriendGroupRepository friendGroupRepository = mock();

        assertThrows(CommandArgumentsCountException.class,
                () -> new CreateGroupCommand(authenticator, friendGroupRepository, new String[2]),
                "Exception should be thrown if there are less arguments than needed");
    }

    @Test
    public void testDoesNotWorkWhenUserIsNotAuthenticated() {
        Authenticator authenticator = mock();
        when(authenticator.isAuthenticated()).thenReturn(false);

        FriendGroupRepository friendGroupRepository = mock();

        Command command =
                new CreateGroupCommand(authenticator, friendGroupRepository, new String[]{"user", "user1", "user2"});
        assertFalse(command.execute(PRINT_WRITER),
                "Command should not work when user is not authenticated");

        verify(friendGroupRepository, times(0))
                .createGroup(anyString(), any());
    }

    @Test
    public void testWorksWhenUserIsAuthenticated() {
        Authenticator authenticator = mock();
        when(authenticator.getAuthenticatedUser())
                .thenReturn(new User("testuser", "pass", "fn", "ln"));
        when(authenticator.isAuthenticated()).thenReturn(true);

        FriendGroupRepository friendGroupRepository = mock();

        Command command =
                new CreateGroupCommand(authenticator, friendGroupRepository, new String[]{"testGroup", "user1", "user2"});

        assertTrue(command.execute(PRINT_WRITER),
                "Command should work when user is authenticated");

        verify(friendGroupRepository, times(1))
                .createGroup("testGroup", Set.of("user1", "user2", "testuser"));
    }

    @Test
    public void testHandlesAlreadyCreated() {
        Authenticator authenticator = mock();
        when(authenticator.getAuthenticatedUser())
                .thenReturn(new User("testuser", "pass", "fn", "ln"));
        when(authenticator.isAuthenticated()).thenReturn(true);

        FriendGroupRepository friendGroupRepository = mock();
        doThrow(GroupAlreadyExistsException.class)
                .when(friendGroupRepository)
                .createGroup(any(), any());
        Command command =
                new CreateGroupCommand(authenticator, friendGroupRepository, new String[]{"testGroup", "user1", "user2"});
        assertThrows(GroupAlreadyExistsException.class, () -> friendGroupRepository.createGroup("testGroup", Set.of("user1", "user2")));
        assertFalse(command.execute(PRINT_WRITER),
                "An exception should be thrown if group already exists");
    }

    @Test
    public void testHelpReturnsCorrectly() {
        ParameterContainer parameters = new ParameterContainer();
        parameters.addParameter("group-name", "the name of the group you wish to create", false);
        parameters.addVariableParameter("user", "the username of a user you want included in the group",
                2, false);
        CommandHelp expected = new CommandHelp("create-group",
                "creates a group, consisting of you and the other users entered in the command",
                parameters);

        CommandHelp actual = CreateGroupCommand.help();

        assertEquals(expected.toString(), actual.toString(),
                "Help method should correct information about the command.");
    }
}
