package bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator.Authenticator;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.Command;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.exception.CommandArgumentsCountException;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.CommandHelp;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.ParameterContainer;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.PersonalExpensesRepository;
import org.junit.jupiter.api.Test;

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
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ExportRecentPersonalExpensesCommandTest {
    private static final PrintWriter PRINT_WRITER = mock();

    @Test
    public void testDoesNotWorkWithInsufficientArguments() {
        Authenticator authenticator = mock();
        PersonalExpensesRepository personalExpensesRepository = mock();

        assertThrows(CommandArgumentsCountException.class,
                () -> new ExportRecentPersonalExpensesCommand(authenticator, personalExpensesRepository, new String[0]),
                "Exception should be thrown if there are less arguments than needed");
        assertThrows(CommandArgumentsCountException.class,
                () -> new ExportRecentPersonalExpensesCommand(authenticator, personalExpensesRepository, new String[3]),
                "Exception should be thrown if there are more arguments than needed");
    }

    @Test
    public void testDoesNotWorkWhenUserIsNotAuthenticated() throws IOException {
        Authenticator authenticator = mock();
        when(authenticator.isAuthenticated()).thenReturn(false);

        PersonalExpensesRepository personalExpensesRepository = mock();
        doAnswer(invocationOnMock -> null)
                .when(personalExpensesRepository)
                .exportRecent(anyString(), anyInt(), any());

        Command command =
                new ExportRecentPersonalExpensesCommand(authenticator, personalExpensesRepository, new String[]{"10", "file"});
        assertFalse(command.execute(PRINT_WRITER),
                "Command should not work when user is not authenticated");

        verify(personalExpensesRepository, times(0))
                .exportRecent(anyString(), anyInt(), any());
    }

    @Test
    public void testDoesNotWorkWhenAmountIsInvalid() throws IOException {
        Authenticator authenticator = mock();
        when(authenticator.getAuthenticatedUser())
                .thenReturn(new User("testuser", "pass", "fn", "ln"));
        when(authenticator.isAuthenticated()).thenReturn(true);

        PersonalExpensesRepository personalExpensesRepository = mock();
        doAnswer(invocationOnMock -> null)
                .when(personalExpensesRepository)
                .exportRecent(anyString(), anyInt(), any());

        Command command =
                new ExportRecentPersonalExpensesCommand(authenticator, personalExpensesRepository, new String[]{"lhjkjh10", "file"});
        assertFalse(command.execute(PRINT_WRITER),
                "Command should not work when amount is invalid");

        verify(personalExpensesRepository, times(0))
                .exportRecent(anyString(), anyInt(), any());
    }

    @Test
    public void testWorksWhenUserIsAuthenticated() throws IOException {
        Authenticator authenticator = mock();
        when(authenticator.getAuthenticatedUser())
                .thenReturn(new User("testuser", "pass", "fn", "ln"));
        when(authenticator.isAuthenticated()).thenReturn(true);

        PersonalExpensesRepository personalExpensesRepository = mock();
        doAnswer(invocationOnMock -> null)
                .when(personalExpensesRepository)
                .exportRecent(anyString(), anyInt(), any());

        Command command =
                new ExportRecentPersonalExpensesCommand(authenticator, personalExpensesRepository, new String[]{"10", "file"});

        assertTrue(command.execute(PRINT_WRITER),
                "Command should work when user is authenticated");

        verify(personalExpensesRepository, times(1))
                .exportRecent(same("testuser"), same(10), any());
    }

    @Test
    public void testHandlesOutputException() throws IOException {
        Authenticator authenticator = mock();
        when(authenticator.getAuthenticatedUser())
                .thenReturn(new User("testuser", "pass", "fn", "ln"));
        when(authenticator.isAuthenticated()).thenReturn(true);

        PersonalExpensesRepository personalExpensesRepository = mock();
        doAnswer(invocationOnMock -> null)
                .when(personalExpensesRepository)
                .exportRecent(anyString(), anyInt(), any());

        doThrow(IOException.class)
                .when(personalExpensesRepository)
                .exportRecent(same("testuser"), same(10), any());
        Command command =
                new ExportRecentPersonalExpensesCommand(authenticator, personalExpensesRepository, new String[]{"10", "file"});
        assertThrows(IOException.class, () -> personalExpensesRepository.exportRecent(authenticator.getAuthenticatedUser().username(), 10, new BufferedWriter(new StringWriter())));
        assertFalse(command.execute(PRINT_WRITER),
                "An exception should be thrown if there are output issues");
    }

    @Test
    public void testHelpReturnsCorrectly() {
        ParameterContainer parameters = new ParameterContainer();
        parameters.addParameter("count", "the amount of expenses you want exported", false);
        parameters.addParameter("filename", "the name of the file you would want the expenses exported to", false);

        CommandHelp expected = new CommandHelp("export-recent-personal-expenses",
                "exports the most recent expenses you have made with specific friends in a specified CSV file",
                parameters);

        CommandHelp actual = ExportRecentPersonalExpensesCommand.help();

        assertEquals(expected.toString(), actual.toString(),
                "Help method should correct information about the command.");
    }
}