package bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator.Authenticator;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.Command;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.exception.CommandArgumentsCountException;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.CommandHelp;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.ParameterContainer;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.GroupDebtsRepository;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import java.io.PrintWriter;

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

public class PayedGroupCommandTest {
    private static final PrintWriter PRINT_WRITER = mock();

    @Test
    public void testDoesNotWorkWithInsufficientArguments() {
        Authenticator authenticator = mock();
        GroupDebtsRepository groupDebtsRepository = mock();

        assertThrows(CommandArgumentsCountException.class,
                () -> new PayedGroupCommand(authenticator, groupDebtsRepository, new String[0]),
                "Exception should be thrown if there are less arguments than needed");
        assertThrows(CommandArgumentsCountException.class,
                () -> new PayedGroupCommand(authenticator, groupDebtsRepository, new String[5]),
                "Exception should be thrown if there are more arguments than needed");
    }

    @Test
    public void testDoesNotWorkWhenUserIsNotAuthenticated() {
        Authenticator authenticator = mock();
        when(authenticator.isAuthenticated()).thenReturn(false);

        GroupDebtsRepository groupDebtsRepository = mock();
        doAnswer((Answer<Void>) invocationOnMock -> null)
                .when(groupDebtsRepository)
                .lowerDebtBurden(any(), any(), any(), anyDouble(), any());

        Command command =
                new PayedGroupCommand(authenticator, groupDebtsRepository, new String[]{"10", "user", "group", "reason"});
        assertFalse(command.execute(PRINT_WRITER),
                "Command should not work when user is not authenticated");

        verify(groupDebtsRepository, times(0))
                .lowerDebtBurden(any(), any(),  any(), anyDouble(), any());
    }

    @Test
    public void testDoesNotWorkWhenAmountIsInvalid() {
        Authenticator authenticator = mock();
        when(authenticator.isAuthenticated()).thenReturn(true);

        GroupDebtsRepository groupDebtsRepository = mock();

        Command command =
                new PayedGroupCommand(authenticator, groupDebtsRepository, new String[]{"-jmb10", "user", "group", "reason"});
        assertFalse(command.execute(PRINT_WRITER),
                "Command should not work when amount is invalid");

        verify(groupDebtsRepository, times(0))
                .lowerDebtBurden(any(), any(), any(),  anyDouble(), any());
    }

    @Test
    public void testWorksWhenUserIsAuthenticated() {
        Authenticator authenticator = mock();
        when(authenticator.getAuthenticatedUser())
                .thenReturn(new User("testuser", "pass", "fn", "ln"));
        when(authenticator.isAuthenticated()).thenReturn(true);

        GroupDebtsRepository groupDebtsRepository = mock();
        doAnswer((Answer<Void>) invocationOnMock -> null)
                .when(groupDebtsRepository)
                .lowerDebtBurden(any(), any(), any(),  anyDouble(), any());

        Command command =
                new PayedGroupCommand(authenticator, groupDebtsRepository, new String[]{"10", "user", "group", "reason"});

        assertTrue(command.execute(PRINT_WRITER),
                "Command should work when user is authenticated");

        verify(groupDebtsRepository, times(1))
                .lowerDebtBurden("user", "testuser", "group",10, "reason");
    }

    @Test
    public void testHandlesErrors() {
        Authenticator authenticator = mock();
        when(authenticator.getAuthenticatedUser())
                .thenReturn(new User("testuser", "pass", "fn", "ln"));
        when(authenticator.isAuthenticated()).thenReturn(true);

        GroupDebtsRepository groupDebtsRepository = mock();
        doThrow(IllegalArgumentException.class)
                .when(groupDebtsRepository)
                .lowerDebtBurden("user", "testuser", "group",10, "reason");
        Command command =
                new PayedGroupCommand(authenticator, groupDebtsRepository, new String[]{"10", "user","group", "reason"});
        assertThrows(IllegalArgumentException.class, () -> groupDebtsRepository.lowerDebtBurden("user", "testuser", "group",10, "reason"));
        assertFalse(command.execute(PRINT_WRITER),
                "An exception should be handled if there are errors");
    }

    @Test
    public void testHelpReturnsCorrectly() {
        ParameterContainer parameters = new ParameterContainer();
        parameters.addParameter("amount", "the amount a user paid you", false);
        parameters.addParameter("username", "the username of the user who paid you", false);
        parameters.addParameter("group-name", "the name of the group in which the debt is active", false);
        parameters.addParameter("reason", "the reason for payment", false);

        CommandHelp expected = new CommandHelp("payed-group",
                "with this command you can mark that a user paid you a given amount for a loan " +
                        "they have to pay to you in a specific group",
                parameters);

        CommandHelp actual = PayedGroupCommand.help();

        assertEquals(expected.toString(), actual.toString(),
                "Help method should correct information about the command.");
    }
}