package bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator.Authenticator;
import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.exception.AlreadyAuthenticatedException;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.Command;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.exception.CommandArgumentsCountException;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.CommandHelp;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.ParameterContainer;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.Notification;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.NotificationType;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.PersonalDebtsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.Set;

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

public class RegisterCommandTest {
    private static final PrintWriter printWriter = mock();

    @Test
    public void testDoesNotWorkWithInsufficientArguments() {
        Authenticator authenticator = mock();
        UserRepository userRepository = mock();

        assertThrows(CommandArgumentsCountException.class,
                () -> new RegisterCommand(authenticator, userRepository, new String[1]),
                "Exception should be thrown if there are less arguments than needed");
        assertThrows(CommandArgumentsCountException.class,
                () -> new RegisterCommand(authenticator, userRepository, new String[3]),
                "Exception should be thrown if there are more arguments than needed");
    }

    @Test
    public void testDoesNotWorkWhenUserIsAuthenticated() {
        Authenticator authenticator = mock();
        when(authenticator.isAuthenticated()).thenReturn(true);

        UserRepository userRepository = mock();
        doAnswer(invocationOnMock -> null)
                .when(userRepository)
                .registerUser(any(), any(), any(), any());

        Command command =
                new RegisterCommand(authenticator, userRepository, new String[]{"username", "pass", "fn", "ln"});
        assertFalse(command.execute(printWriter),
                "Command should not work when user is authenticated");

        verify(userRepository, times(0))
                .registerUser(any(), any(), any(), any());
    }

    @Test
    public void testWorks() throws AlreadyAuthenticatedException {
        Authenticator authenticator = mock();
        when(authenticator.isAuthenticated()).thenReturn(false);

        UserRepository userRepository = mock();
        doAnswer(invocationOnMock -> null)
                .when(userRepository)
                .registerUser(any(), any(), any(), any());

        Command command =
                new RegisterCommand(authenticator, userRepository, new String[]{"username", "pass", "fn", "ln"});

        assertTrue(command.execute(printWriter),
                "Command should work when user is authenticated");

        verify(userRepository, times(1))
                .registerUser("username", "pass", "fn", "ln");
    }

    @Test
    public void testHandlesErrors() throws AlreadyAuthenticatedException {
        Authenticator authenticator = mock();
        when(authenticator.isAuthenticated()).thenReturn(false);

        UserRepository userRepository = mock();
        doThrow(IllegalArgumentException.class)
                .when(userRepository)
                .registerUser(any(), any(), any(), any());
        Command command =
                new RegisterCommand(authenticator, userRepository, new String[]{"username", "pass", "fn", "ln"});
        assertThrows(IllegalArgumentException.class, () -> userRepository.registerUser("username", "pass", "fn", "ln"));
        assertFalse(command.execute(printWriter),
                "An exception should be thrown if there are any errors");
    }

    @Test
    public void testHelpReturnsCorrectly() {
        ParameterContainer parameters = new ParameterContainer();
        parameters.addParameter("username", "your username", false);
        parameters.addParameter("password", "your password", false);
        parameters.addParameter("first-name", "your first name", false);
        parameters.addParameter("last-name", "your last name", false);

        CommandHelp expected = new CommandHelp("register",
                "registers you in the system",
                parameters);
        CommandHelp actual = RegisterCommand.help();

        assertEquals(expected.toString(), actual.toString(),
                "Help method should correct information about the command.");
    }
}
