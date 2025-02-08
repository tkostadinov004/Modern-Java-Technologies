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

public class LoginCommandTest {
    private static final PrintWriter PRINT_WRITER = mock();

    @Test
    public void testDoesNotWorkWithInsufficientArguments() {
        Authenticator authenticator = mock();
        NotificationsRepository notificationsRepository = mock();

        assertThrows(CommandArgumentsCountException.class,
                () -> new LoginCommand(authenticator, notificationsRepository, new String[1]),
                "Exception should be thrown if there are less arguments than needed");
        assertThrows(CommandArgumentsCountException.class,
                () -> new LoginCommand(authenticator, notificationsRepository, new String[3]),
                "Exception should be thrown if there are more arguments than needed");
    }

    @Test
    public void testWorks() throws AlreadyAuthenticatedException {
        Authenticator authenticator = mock();
        doAnswer((Answer<Void>) invocationOnMock -> null)
                .when(authenticator)
                .authenticate(anyString(), anyString());

        NotificationsRepository notificationsRepository = mock();
        when(notificationsRepository.getNotificationsForUser("testuser"))
                .thenReturn(Set.of(new Notification(new User("testuser", "pass", "fn", "ln"), "asd", LocalDateTime.now(), NotificationType.PERSONAL)));

        Command command =
                new LoginCommand(authenticator, notificationsRepository, new String[]{"testuser", "testpass"});

        assertTrue(command.execute(PRINT_WRITER),
                "Command should work when user is authenticated");

        verify(notificationsRepository, times(1))
                .getNotificationsForUser("testuser");
    }

    @Test
    public void testHandlesErrors() throws AlreadyAuthenticatedException {
        Authenticator authenticator = mock();
        when(authenticator.getAuthenticatedUser())
                .thenReturn(new User("testuser", "pass", "fn", "ln"));
        when(authenticator.isAuthenticated()).thenReturn(true);

        NotificationsRepository notificationsRepository = mock();
        doThrow(AlreadyAuthenticatedException.class)
                .when(authenticator)
                .authenticate(anyString(), anyString());
        Command command =
                new LoginCommand(authenticator, notificationsRepository, new String[]{"testuser", "testpass"});
        assertThrows(AlreadyAuthenticatedException.class, () -> authenticator.authenticate("testuser", "testpass"));
        assertFalse(command.execute(PRINT_WRITER),
                "An exception should be thrown if there are any errors");
    }

    @Test
    public void testHelpReturnsCorrectly() {
        ParameterContainer parameters = new ParameterContainer();
        parameters.addParameter("username", "your username", false);
        parameters.addParameter("password", "your password", false);

        CommandHelp expected =  new CommandHelp("login",
                "logs you in the system",
                parameters);

        CommandHelp actual = LoginCommand.help();

        assertEquals(expected.toString(), actual.toString(),
                "Help method should correct information about the command.");
    }
}
