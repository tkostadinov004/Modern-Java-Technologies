package bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator.Authenticator;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.Command;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.exception.CommandArgumentsCountException;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.CommandHelp;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.ParameterContainer;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.PersonalDebtsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.AlreadyFriendsException;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.PrintWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PayedCommandTest {
    private static final PrintWriter printWriter = mock();

    @Test
    public void testDoesNotWorkWithInsufficientArguments() {
        Authenticator authenticator = mock();
        PersonalDebtsRepository personalDebtsRepository = mock();

        assertThrows(CommandArgumentsCountException.class,
                () -> new PayedCommand(authenticator, personalDebtsRepository, new String[0]),
                "Exception should be thrown if there are less arguments than needed");
        assertThrows(CommandArgumentsCountException.class,
                () -> new PayedCommand(authenticator, personalDebtsRepository, new String[4]),
                "Exception should be thrown if there are more arguments than needed");
    }

    @Test
    public void testDoesNotWorkWhenUserIsNotAuthenticated() {
        Authenticator authenticator = mock();
        when(authenticator.isAuthenticated()).thenReturn(false);

        PersonalDebtsRepository personalDebtsRepository = mock();
        doAnswer((Answer<Void>) invocationOnMock -> null)
                .when(personalDebtsRepository)
                .lowerDebtBurden(any(), any(), anyDouble(), any());

        Command command =
                new PayedCommand(authenticator, personalDebtsRepository, new String[]{"10", "user", "reason"});
        assertFalse(command.execute(printWriter),
                "Command should not work when user is not authenticated");

        verify(personalDebtsRepository, times(0))
                .lowerDebtBurden(any(), any(), anyDouble(), any());
    }

    @Test
    public void testDoesNotWorkWhenAmountIsInvalid() {
        Authenticator authenticator = mock();
        when(authenticator.isAuthenticated()).thenReturn(true);

        PersonalDebtsRepository personalDebtsRepository = mock();

        Command command =
                new PayedCommand(authenticator, personalDebtsRepository, new String[]{"-1asdasd0", "user", "reason"});
        assertFalse(command.execute(printWriter),
                "Command should not work when amount is invalid");

        verify(personalDebtsRepository, times(0))
                .lowerDebtBurden(any(), any(), anyDouble(), any());
    }

    @Test
    public void testWorksWhenUserIsAuthenticated() {
        Authenticator authenticator = mock();
        when(authenticator.getAuthenticatedUser())
                .thenReturn(new User("testuser", "pass", "fn", "ln"));
        when(authenticator.isAuthenticated()).thenReturn(true);

        PersonalDebtsRepository personalDebtsRepository = mock();
        doAnswer((Answer<Void>) invocationOnMock -> null)
                .when(personalDebtsRepository)
                .lowerDebtBurden(any(), any(), anyDouble(), any());

        Command command =
                new PayedCommand(authenticator, personalDebtsRepository, new String[]{"10", "user", "reason"});

        assertTrue(command.execute(printWriter),
                "Command should work when user is authenticated");

        verify(personalDebtsRepository, times(1))
                .lowerDebtBurden("user", "testuser", 10, "reason");
    }

    @Test
    public void testHandlesErrors() {
        Authenticator authenticator = mock();
        when(authenticator.getAuthenticatedUser())
                .thenReturn(new User("testuser", "pass", "fn", "ln"));
        when(authenticator.isAuthenticated()).thenReturn(true);

        PersonalDebtsRepository personalDebtsRepository = mock();
        doThrow(IllegalArgumentException.class)
                .when(personalDebtsRepository)
                .lowerDebtBurden("user", "testuser", 10, "reason");
        Command command =
                new PayedCommand(authenticator, personalDebtsRepository, new String[]{"10", "user", "reason"});
        assertThrows(IllegalArgumentException.class, () -> personalDebtsRepository.lowerDebtBurden("user", "testuser", 10, "reason"));
        assertFalse(command.execute(printWriter),
                "An exception should be handled if there are errors");
    }

    @Test
    public void testHelpReturnsCorrectly() {
        ParameterContainer parameters = new ParameterContainer();
        parameters.addParameter("amount", "the amount a user paid you", false);
        parameters.addParameter("username", "the username of the user who paid you", false);
        parameters.addParameter("reason", "the reason for payment", false);

        CommandHelp expected = new CommandHelp("payed",
                "with this command you can mark that a user paid you a given amount for a loan he has to pay to you",
                parameters);

        CommandHelp actual = PayedCommand.help();

        assertEquals(expected.toString(), actual.toString(),
                "Help method should correct information about the command.");
    }
}