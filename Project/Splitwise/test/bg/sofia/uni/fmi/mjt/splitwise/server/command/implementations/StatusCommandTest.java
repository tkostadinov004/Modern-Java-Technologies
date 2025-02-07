package bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator.Authenticator;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.Command;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.exception.CommandArgumentsCountException;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.CommandHelp;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.ParameterContainer;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.FriendGroup;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.GroupDebt;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.PersonalDebt;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.GroupDebtsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.PersonalDebtsRepository;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
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

public class StatusCommandTest {
    private static final PrintWriter printWriter = mock();

    @Test
    public void testDoesNotWorkWithInsufficientArguments() {
        Authenticator authenticator = mock();
        PersonalDebtsRepository personalDebtsRepository = mock();
        GroupDebtsRepository groupDebtsRepository = mock();

        assertThrows(CommandArgumentsCountException.class,
                () -> new StatusCommand(authenticator, personalDebtsRepository, groupDebtsRepository, new String[1]),
                "Exception should be thrown if there are more arguments than needed");
    }

    @Test
    public void testDoesNotWorkWhenUserIsNotAuthenticated() {
        Authenticator authenticator = mock();
        when(authenticator.isAuthenticated()).thenReturn(false);

        PersonalDebtsRepository personalDebtsRepository = mock();
        GroupDebtsRepository groupDebtsRepository = mock();

        Command command =
                new StatusCommand(authenticator, personalDebtsRepository, groupDebtsRepository, new String[0]);
        assertFalse(command.execute(printWriter),
                "Command should not work when user is not authenticated");

        verify(personalDebtsRepository, times(0))
                .getDebtsOf(anyString());
        verify(groupDebtsRepository, times(0))
                .getDebtsOf(anyString());
    }

    @Test
    public void testWorksWhenUserIsAuthenticated() {
        User user1 = new User("testuser", "pass", "fn", "ln");
        User user2 = new User("testuser1", "pass", "fn", "ln");
        FriendGroup group = new FriendGroup("testGroup", Set.of(user1, user2));

        Authenticator authenticator = mock();
        when(authenticator.getAuthenticatedUser())
                .thenReturn(user1);
        when(authenticator.isAuthenticated()).thenReturn(true);

        PersonalDebtsRepository personalDebtsRepository = mock();
        when(personalDebtsRepository.getDebtsOf("testuser"))
                .thenReturn(Set.of(new PersonalDebt(user1, user2, 100, "reason"), new PersonalDebt(user2, user1, 150, "reason2")));
        GroupDebtsRepository groupDebtsRepository = mock();
        Map<FriendGroup, Set<GroupDebt>> groupDebts = new HashMap<>();
        groupDebts.put(group, Set.of(new GroupDebt(user1, user2, group,100, "reason"), new GroupDebt(user2, user1, group,150, "reason2")));
        when(groupDebtsRepository.getDebtsOf("testuser"))
                .thenReturn(groupDebts);

        Command command =
                new StatusCommand(authenticator, personalDebtsRepository, groupDebtsRepository, new String[0]);

        assertTrue(command.execute(printWriter),
                "Command should work when user is authenticated");

        verify(personalDebtsRepository, times(1))
                .getDebtsOf("testuser");
        verify(groupDebtsRepository, times(1))
                .getDebtsOf("testuser");
    }

    @Test
    public void testHandlesErrors() {
        Authenticator authenticator = mock();
        when(authenticator.getAuthenticatedUser())
                .thenReturn(new User("testuser", "pass", "fn", "ln"));
        when(authenticator.isAuthenticated()).thenReturn(true);

        PersonalDebtsRepository personalDebtsRepository = mock();
        GroupDebtsRepository groupDebtsRepository = mock();
        doThrow(IllegalArgumentException.class)
                .when(personalDebtsRepository)
                .getDebtsOf("testuser");
        Command command =
                new StatusCommand(authenticator, personalDebtsRepository, groupDebtsRepository, new String[0]);
        assertThrows(IllegalArgumentException.class, () -> personalDebtsRepository.getDebtsOf("testuser"));
        assertFalse(command.execute(printWriter),
                "Exceptions should be handled");
    }

    @Test
    public void testHelpReturnsCorrectly() {
        CommandHelp expected = new CommandHelp("get-status",
                "prints the people you owe money to and the people who owe money to you",
                new ParameterContainer());

        CommandHelp actual = StatusCommand.help();

        assertEquals(expected.toString(), actual.toString(),
                "Help method should correct information about the command.");
    }
}

