package bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator.Authenticator;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.Command;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.exception.CommandArgumentsCountException;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.CommandHelp;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.ParameterContainer;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.GroupExpensesRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.AlreadyFriendsException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ExportRecentGroupExpensesCommandTest {
    private static final PrintWriter printWriter = mock();

    @Test
    public void testDoesNotWorkWithInsufficientArguments() {
        Authenticator authenticator = mock();
        GroupExpensesRepository groupExpensesRepository = mock();

        assertThrows(CommandArgumentsCountException.class,
                () -> new ExportRecentGroupExpensesCommand(authenticator, groupExpensesRepository, new String[0]),
                "Exception should be thrown if there are less arguments than needed");
        assertThrows(CommandArgumentsCountException.class,
                () -> new ExportRecentGroupExpensesCommand(authenticator, groupExpensesRepository, new String[3]),
                "Exception should be thrown if there are more arguments than needed");
    }

    @Test
    public void testDoesNotWorkWhenUserIsNotAuthenticated() throws IOException {
        Authenticator authenticator = mock();
        when(authenticator.isAuthenticated()).thenReturn(false);

        GroupExpensesRepository groupExpensesRepository = mock();

        Command command =
                new ExportRecentGroupExpensesCommand(authenticator, groupExpensesRepository, new String[]{ "10", "file"});
        assertFalse(command.execute(printWriter),
                "Command should not work when user is not authenticated");

        verify(groupExpensesRepository, times(0))
                .exportRecent(anyString(), anyInt(), any());
    }

    @Test
    public void testDoesNotWorkWhenAmountIsInvalid() throws IOException {
        Authenticator authenticator = mock();
        when(authenticator.getAuthenticatedUser())
                .thenReturn(new User("testuser", "pass", "fn", "ln"));
        when(authenticator.isAuthenticated()).thenReturn(true);

        GroupExpensesRepository groupExpensesRepository = mock();

        Command command =
                new ExportRecentGroupExpensesCommand(authenticator, groupExpensesRepository, new String[]{ "lhjkjh10", "file"});
        assertFalse(command.execute(printWriter),
                "Command should not work when amount is invalid");

        verify(groupExpensesRepository, times(0))
                .exportRecent(anyString(), anyInt(), any());
    }

    @Test
    public void testWorksWhenUserIsAuthenticated() throws IOException {
        Authenticator authenticator = mock();
        when(authenticator.getAuthenticatedUser())
                .thenReturn(new User("testuser", "pass", "fn", "ln"));
        when(authenticator.isAuthenticated()).thenReturn(true);

        GroupExpensesRepository groupExpensesRepository = mock();

        Command command =
                new ExportRecentGroupExpensesCommand(authenticator, groupExpensesRepository, new String[]{ "10", "file"});

        assertTrue(command.execute(printWriter),
                "Command should work when user is authenticated");

        verify(groupExpensesRepository, times(1))
                .exportRecent(same("testuser"), same(10), any());
    }

    @Test
    public void testHandlesOutputException() throws IOException {
        Authenticator authenticator = mock();
        when(authenticator.getAuthenticatedUser())
                .thenReturn(new User("testuser", "pass", "fn", "ln"));
        when(authenticator.isAuthenticated()).thenReturn(true);

        GroupExpensesRepository groupExpensesRepository = mock();
        doThrow(IOException.class)
                .when(groupExpensesRepository)
                .exportRecent(same("testuser"), same(10), any());
        Command command =
                new ExportRecentGroupExpensesCommand(authenticator, groupExpensesRepository, new String[]{ "10", "file"});
        assertThrows(IOException.class, () -> groupExpensesRepository.exportRecent(authenticator.getAuthenticatedUser().username(), 10, new BufferedWriter(new StringWriter())));
        assertFalse(command.execute(printWriter),
                "An exception should be thrown if there are output issues");
    }

    @Test
    public void testHelpReturnsCorrectly() {
        ParameterContainer parameters = new ParameterContainer();
        parameters.addParameter("count", "the amount of expenses you want exported", false);
        parameters.addParameter("filename", "the name of the file you would want the expenses exported to", false);

        CommandHelp expected = new CommandHelp("export-recent-group-expenses",
                "exports the most recent expenses you have made in your groups in a specified CSV file",
                parameters);

        CommandHelp actual = ExportRecentGroupExpensesCommand.help();

        assertEquals(expected.toString(), actual.toString(),
                "Help method should correct information about the command.");
    }
}
