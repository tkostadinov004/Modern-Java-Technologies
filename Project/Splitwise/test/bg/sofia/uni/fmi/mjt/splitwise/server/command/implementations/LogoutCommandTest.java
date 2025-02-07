package bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator.Authenticator;
import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.exception.AlreadyAuthenticatedException;
import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.exception.NotAuthenticatedException;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.Command;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.exception.CommandArgumentsCountException;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.CommandHelp;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.ParameterContainer;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.Notification;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.NotificationType;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.NotificationsRepository;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LogoutCommandTest {
    private static final PrintWriter printWriter = mock();

    @Test
    public void testDoesNotWorkWithInsufficientArguments() {
        Authenticator authenticator = mock();

        assertThrows(CommandArgumentsCountException.class,
                () -> new LogoutCommand(authenticator, new String[1]),
                "Exception should be thrown if there are more arguments than needed");
    }

    @Test
    public void testWorks() throws NotAuthenticatedException {
        Authenticator authenticator = mock();
        doAnswer((Answer<Void>) invocationOnMock -> null)
                .when(authenticator)
                .logout();

        Command command =
                new LogoutCommand(authenticator, new String[0]);

        assertTrue(command.execute(printWriter),
                "Command should work when user is authenticated");
    }

    @Test
    public void testHandlesErrors() throws NotAuthenticatedException {
        Authenticator authenticator = mock();
        doThrow(NotAuthenticatedException.class)
                .when(authenticator)
                .logout();
        Command command =
                new LogoutCommand(authenticator, new String[0]);
        assertThrows(NotAuthenticatedException.class, () -> authenticator.logout());
        assertFalse(command.execute(printWriter),
                "An exception should be thrown if there are any errors");
    }

    @Test
    public void testHelpReturnsCorrectly() {
        CommandHelp expected = new CommandHelp("logout",
                "logs you out of the system",
                new ParameterContainer());

        CommandHelp actual = LogoutCommand.help();

        assertEquals(expected.toString(), actual.toString(),
                "Help method should correct information about the command.");
    }
}