package bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator.Authenticator;
import bg.sofia.uni.fmi.mjt.splitwise.server.chat.exception.ChatException;
import bg.sofia.uni.fmi.mjt.splitwise.server.chat.token.ChatToken;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.Command;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.exception.CommandArgumentsCountException;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.CommandHelp;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.ParameterContainer;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.NotificationsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.NonExistentUserException;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.PrintWriter;

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

public class ExitChatCommandTest {
    private static final PrintWriter printWriter = mock();

    @Test
    public void testDoesNotWorkWithInsufficientArguments() {
        Authenticator authenticator = mock();
        ChatToken chatToken = mock();

        assertThrows(CommandArgumentsCountException.class,
                () -> new ExitChatCommand(authenticator, chatToken, new String[1]),
                "Exception should be thrown if there are more arguments than needed");
    }

    @Test
    public void testDoesNotWorkWhenUserIsNotAuthenticated() throws ChatException {
        Authenticator authenticator = mock();
        when(authenticator.isAuthenticated()).thenReturn(false);

        ChatToken chatToken = mock();

        Command command =
                new ExitChatCommand(authenticator, chatToken, new String[0]);
        assertFalse(command.execute(printWriter),
                "Command should not work when user is not authenticated");

        verify(chatToken, times(0))
                .leaveChat();
    }

    @Test
    public void testDoesNotWorkWhenUserIsNotInChat() throws ChatException {
        Authenticator authenticator = mock();
        when(authenticator.getAuthenticatedUser())
                .thenReturn(new User("testuser", "pass", "fn", "ln"));
        when(authenticator.isAuthenticated()).thenReturn(true);

        ChatToken chatToken = mock();
        when(chatToken.isInChat()).thenReturn(false);

        Command command =
                new ExitChatCommand(authenticator, chatToken, new String[0]);
        assertFalse(command.execute(printWriter),
                "Command should not work when user is not in chat");

        verify(chatToken, times(0))
                .leaveChat();
    }

    @Test
    public void testWorksWhenUserIsAuthenticatedAndInChat() throws ChatException{
        Authenticator authenticator = mock();
        when(authenticator.getAuthenticatedUser())
                .thenReturn(new User("testuser", "pass", "fn", "ln"));
        when(authenticator.isAuthenticated()).thenReturn(true);

        ChatToken chatToken = mock();
        when(chatToken.isInChat()).thenReturn(true);
        doAnswer((Answer<Void>) invocationOnMock -> null)
                .when(chatToken)
                .leaveChat();

        Command command =
                new ExitChatCommand(authenticator, chatToken, new String[0]);

        assertTrue(command.execute(printWriter),
                "Command should work when user is authenticated and in chat");

        verify(chatToken, times(1))
                .leaveChat();
    }

    @Test
    public void testHandlesChatException() throws ChatException {
        Authenticator authenticator = mock();
        when(authenticator.getAuthenticatedUser())
                .thenReturn(new User("testuser", "pass", "fn", "ln"));
        when(authenticator.isAuthenticated()).thenReturn(true);

        ChatToken chatToken = mock();
        when(chatToken.isInChat()).thenReturn(true);
        doThrow(ChatException.class)
                .when(chatToken)
                .leaveChat();

        Command command =
                new ExitChatCommand(authenticator, chatToken, new String[0]);
        assertThrows(ChatException.class, () -> chatToken.leaveChat());
        assertFalse(command.execute(printWriter),
                "An exception should be thrown if there is a chat error");
    }

    @Test
    public void testHelpReturnsCorrectly() {
        CommandHelp expected = new CommandHelp("exit-chat",
                "leaves the chat room you are in. you can later rejoin by using the \"join-room\" " +
                        "command and the room code",
                new ParameterContainer());

        CommandHelp actual = ExitChatCommand.help();

        assertEquals(expected.toString(), actual.toString(),
                "Help method should correct information about the command.");
    }
}

