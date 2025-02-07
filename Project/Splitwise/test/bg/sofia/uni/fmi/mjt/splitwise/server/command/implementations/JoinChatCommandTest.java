package bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator.Authenticator;
import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.exception.NotAuthenticatedException;
import bg.sofia.uni.fmi.mjt.splitwise.server.chat.exception.ChatException;
import bg.sofia.uni.fmi.mjt.splitwise.server.chat.token.ChatToken;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.Command;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.exception.CommandArgumentsCountException;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.CommandHelp;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.ParameterContainer;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JoinChatCommandTest {
    private static final PrintWriter printWriter = mock();

    @Test
    public void testDoesNotWorkWithInsufficientArguments() {
        Authenticator authenticator = mock();
        ChatToken chatToken = mock();

        assertThrows(CommandArgumentsCountException.class,
                () -> new JoinChatCommand(authenticator, chatToken, new String[0]),
                "Exception should be thrown if there are less arguments than needed");
        assertThrows(CommandArgumentsCountException.class,
                () -> new JoinChatCommand(authenticator, chatToken, new String[2]),
                "Exception should be thrown if there are more arguments than needed");
    }

    @Test
    public void testDoesNotWorkWhenUserIsNotAuthenticated() throws ChatException, NotAuthenticatedException {
        Authenticator authenticator = mock();
        when(authenticator.isAuthenticated()).thenReturn(false);

        ChatToken chatToken = mock();

        Command command =
                new JoinChatCommand(authenticator, chatToken, new String[]{"test-code"});
        assertFalse(command.execute(printWriter),
                "Command should not work when user is not authenticated");

        verify(chatToken, times(0))
                .joinChat(any());
    }

    @Test
    public void testDoesNotWorkWhenUserIsInChat() throws ChatException {
        Authenticator authenticator = mock();
        when(authenticator.isAuthenticated()).thenReturn(true);

        ChatToken chatToken = mock();
        when(chatToken.isInChat()).thenReturn(true);

        Command command =
                new JoinChatCommand(authenticator, chatToken, new String[]{"test-code"});
        assertFalse(command.execute(printWriter),
                "Command should not work when user is in a chat");

        verify(chatToken, times(0))
                .joinChat(any());
    }

    @Test
    public void testWorksWhenUserIsAuthenticated() throws ChatException, NotAuthenticatedException {
        Authenticator authenticator = mock();
        when(authenticator.getAuthenticatedUser())
                .thenReturn(new User("testuser", "pass", "fn", "ln"));
        when(authenticator.isAuthenticated()).thenReturn(true);

        ChatToken chatToken = mock();
        when(chatToken.isInChat()).thenReturn(false);

        String code = "asd-asd-asd";
        Command command =
                new JoinChatCommand(authenticator, chatToken, new String[]{code});

        assertTrue(command.execute(printWriter),
                "Command should work when user is authenticated and not in chat");

        verify(chatToken, times(1))
                .joinChat(code);
    }

    @Test
    public void testHandlesChatException() throws ChatException{
        Authenticator authenticator = mock();
        when(authenticator.getAuthenticatedUser())
                .thenReturn(new User("testuser", "pass", "fn", "ln"));
        when(authenticator.isAuthenticated()).thenReturn(true);

        ChatToken chatToken = mock();
        doThrow(ChatException.class)
                .when(chatToken)
                .joinChat(any());
        Command command =
                new JoinChatCommand(authenticator, chatToken, new String[]{"asdasd"});
        assertThrows(ChatException.class, () -> chatToken.joinChat("asdasd"));
        assertFalse(command.execute(printWriter),
                "Command should handle server errors");
    }

    @Test
    public void testHandlesServerErrors() throws ChatException{
        Authenticator authenticator = mock();
        when(authenticator.getAuthenticatedUser())
                .thenReturn(new User("testuser", "pass", "fn", "ln"));
        when(authenticator.isAuthenticated()).thenReturn(true);

        ChatToken chatToken = mock();
        doThrow(RuntimeException.class)
                .when(chatToken)
                .joinChat(any());
        Command command =
                new JoinChatCommand(authenticator, chatToken, new String[]{"asdasd"});
        assertThrows(RuntimeException.class, () -> chatToken.joinChat("asdasd"));
        assertFalse(command.execute(printWriter),
                "Command should handle server errors");
    }

    @Test
    public void testHelpReturnsCorrectly() {
        ParameterContainer parameters = new ParameterContainer();
        parameters.addParameter("chat-code", "the code of the chat room you wish to join", false);

        CommandHelp expected = new CommandHelp("join-chat",
                "joins a given chat room, identified by the provided unique chat room code",
                parameters);

        CommandHelp actual = JoinChatCommand.help();

        assertEquals(expected.toString(), actual.toString(),
                "Help method should correct information about the command.");
    }
}