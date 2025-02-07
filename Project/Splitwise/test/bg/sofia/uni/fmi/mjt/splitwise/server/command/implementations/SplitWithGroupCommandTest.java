package bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator.Authenticator;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.Command;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.exception.CommandArgumentsCountException;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.CommandHelp;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.ParameterContainer;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.GroupExpensesRepository;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import java.io.PrintWriter;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SplitWithGroupCommandTest {
    private static final PrintWriter printWriter = mock();

    @Test
    public void testDoesNotWorkWithInsufficientArguments() {
        Authenticator authenticator = mock();
        GroupExpensesRepository groupDebtsRepository = mock();

        assertThrows(CommandArgumentsCountException.class,
                () -> new SplitWithGroupCommand(authenticator, groupDebtsRepository, new String[0]),
                "Exception should be thrown if there are less arguments than needed");
        assertThrows(CommandArgumentsCountException.class,
                () -> new SplitWithGroupCommand(authenticator, groupDebtsRepository, new String[5]),
                "Exception should be thrown if there are more arguments than needed");
    }

    @Test
    public void testDoesNotWorkWhenUserIsNotAuthenticated() {
        Authenticator authenticator = mock();
        when(authenticator.isAuthenticated()).thenReturn(false);

        GroupExpensesRepository groupDebtsRepository = mock();
        doAnswer((Answer<Void>) invocationOnMock -> null)
                .when(groupDebtsRepository)
                .addExpense(any(), any(),  anyDouble(), any(), any());

        Command command =
                new SplitWithGroupCommand(authenticator, groupDebtsRepository, new String[]{"10", "group", "reason"});
        assertFalse(command.execute(printWriter),
                "Command should not work when user is not authenticated");

        verify(groupDebtsRepository, times(0))
                .addExpense(any(), any(),  anyDouble(), any(), any());
    }

    @Test
    public void testDoesNotWorkWhenAmountIsInvalid() {
        Authenticator authenticator = mock();
        when(authenticator.isAuthenticated()).thenReturn(true);

        GroupExpensesRepository groupDebtsRepository = mock();

        Command command =
                new SplitWithGroupCommand(authenticator, groupDebtsRepository, new String[]{"-jmb10", "group", "reason"});
        assertFalse(command.execute(printWriter),
                "Command should not work when amount is invalid");

        verify(groupDebtsRepository, times(0))
                .addExpense(any(), any(),  anyDouble(), any(), any());
    }

    @Test
    public void testWorksWhenUserIsAuthenticated() {
        Authenticator authenticator = mock();
        when(authenticator.getAuthenticatedUser())
                .thenReturn(new User("testuser", "pass", "fn", "ln"));
        when(authenticator.isAuthenticated()).thenReturn(true);

        GroupExpensesRepository groupDebtsRepository = mock();
        doAnswer((Answer<Void>) invocationOnMock -> null)
                .when(groupDebtsRepository)
                .addExpense(any(), any(),  anyDouble(), any(), any());

        Command command =
                new SplitWithGroupCommand(authenticator, groupDebtsRepository, new String[]{"10", "group", "reason"});

        assertTrue(command.execute(printWriter),
                "Command should work when user is authenticated");

        verify(groupDebtsRepository, times(1))
                .addExpense(any(), any(),  anyDouble(), any(), any());
    }

    @Test
    public void testHandlesErrors() {
        Authenticator authenticator = mock();
        when(authenticator.getAuthenticatedUser())
                .thenReturn(new User("testuser", "pass", "fn", "ln"));
        when(authenticator.isAuthenticated()).thenReturn(true);

        GroupExpensesRepository groupDebtsRepository = mock();
        doThrow(IllegalArgumentException.class)
                .when(groupDebtsRepository)
                .addExpense(any(), any(), anyDouble(), any(), any());
        Command command =
                new SplitWithGroupCommand(authenticator, groupDebtsRepository, new String[]{"10", "group", "reason"});
        assertThrows(IllegalArgumentException.class, () -> groupDebtsRepository.addExpense("testuser", "group", 10, "reason", LocalDateTime.now()));
        assertFalse(command.execute(printWriter),
                "An exception should be handled if there are errors");
    }

    @Test
    public void testHelpReturnsCorrectly() {
        ParameterContainer parameters = new ParameterContainer();
        parameters.addParameter("amount", "the amount a user should pay you", false);
        parameters.addParameter("group-name", "the name of the group with which you split your bill", false);
        parameters.addParameter("reason", "the reason for splitting", false);

        CommandHelp expected = new CommandHelp("split-group",
                "with this command you can mark that a all users owe you " +
                        "an equal amount of money for a specific reason",
                parameters);

        CommandHelp actual = SplitWithGroupCommand.help();

        assertEquals(expected.toString(), actual.toString(),
                "Help method should correct information about the command.");
    }
}
