package bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator.Authenticator;
import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.exception.NotAuthenticatedException;
import bg.sofia.uni.fmi.mjt.splitwise.server.chat.DefaultChatServer;
import bg.sofia.uni.fmi.mjt.splitwise.server.chat.exception.ChatException;
import bg.sofia.uni.fmi.mjt.splitwise.server.chat.token.ChatToken;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.Command;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.exception.CommandArgumentsCountException;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.CommandHelp;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.ParameterContainer;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.ChatRepository;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SendMessageInChatCommandTest {
    private static final PrintWriter printWriter = mock();

    @Test
    public void testDoesNotWorkWithInsufficientArguments() {
        Authenticator authenticator = mock();
        ChatToken chatToken = mock();
        ChatRepository chatRepository = mock();

        assertThrows(CommandArgumentsCountException.class,
                () -> new SendMessageInChatCommand(authenticator, chatToken, chatRepository, new String[0]),
                "Exception should be thrown if there are less arguments than needed");
        assertThrows(CommandArgumentsCountException.class,
                () -> new SendMessageInChatCommand(authenticator, chatToken, chatRepository, new String[2]),
                "Exception should be thrown if there are more arguments than needed");
    }

    @Test
    public void testDoesNotWorkWhenUserIsNotAuthenticated() throws ChatException, NotAuthenticatedException {
        Authenticator authenticator = mock();
        when(authenticator.isAuthenticated()).thenReturn(false);

        ChatToken chatToken = mock();
        ChatRepository chatRepository = mock();
        doAnswer(invocationOnMock -> null)
                .when(chatRepository)
                .sendMessage(any(), any(), any());

        Command command =
                new SendMessageInChatCommand(authenticator, chatToken, chatRepository, new String[]{"message"});
        assertFalse(command.execute(printWriter),
                "Command should not work when user is not authenticated");

        verify(chatRepository, times(0))
                .sendMessage(any(), any(), any());
    }

    @Test
    public void testDoesNotWorkWhenUserIsNotInChat() throws ChatException {
        Authenticator authenticator = mock();
        when(authenticator.isAuthenticated()).thenReturn(true);

        ChatToken chatToken = mock();
        when(chatToken.isInChat()).thenReturn(false);

        ChatRepository chatRepository = mock();
        doAnswer(invocationOnMock -> null)
                .when(chatRepository)
                .sendMessage(any(), any(), any());

        Command command =
                new SendMessageInChatCommand(authenticator, chatToken, chatRepository, new String[]{"message"});
        assertFalse(command.execute(printWriter),
                "Command should not work when user is not in a chat");

        verify(chatRepository, times(0))
                .sendMessage(any(), any(), any());
    }

    @Test
    public void testWorksWhenUserIsAuthenticatedAndInChat() throws ChatException, NotAuthenticatedException {
        Authenticator authenticator = mock();
        when(authenticator.getAuthenticatedUser())
                .thenReturn(new User("testuser", "pass", "fn", "ln"));
        when(authenticator.isAuthenticated()).thenReturn(true);

        ChatToken chatToken = mock();
        when(chatToken.isInChat()).thenReturn(true);
        when(chatToken.getServer())
                .thenReturn(new DefaultChatServer(new InetSocketAddress("asd", 123), "testcode"));

        ChatRepository chatRepository = mock();
        doAnswer(invocationOnMock -> null)
                .when(chatRepository)
                .sendMessage(any(), any(), any());

        Command command =
                new SendMessageInChatCommand(authenticator, chatToken, chatRepository, new String[]{"message"});

        assertTrue(command.execute(printWriter),
                "Command should work when user is authenticated and in chat");

        verify(chatRepository, times(1))
                .sendMessage(any(), any(), any());
    }

    @Test
    public void testHandlesChatException() throws ChatException {
        Authenticator authenticator = mock();
        when(authenticator.getAuthenticatedUser())
                .thenReturn(new User("testuser", "pass", "fn", "ln"));
        when(authenticator.isAuthenticated()).thenReturn(true);

        ChatToken chatToken = mock();
        when(chatToken.isInChat()).thenReturn(true);
        when(chatToken.getServer())
                .thenReturn(new DefaultChatServer(new InetSocketAddress("asd", 123), "testcode"));

        ChatRepository chatRepository = mock();
        doThrow(IllegalArgumentException.class)
                .when(chatRepository)
                .sendMessage(any(), any(), any());

        Command command =
                new SendMessageInChatCommand(authenticator, chatToken, chatRepository, new String[]{"asdasd"});
        assertThrows(IllegalArgumentException.class, () -> chatRepository.sendMessage("asdasd", "asdasd", "message"));
        assertFalse(command.execute(printWriter),
                "Command should handle errors");
    }

    @Test
    public void testHelpReturnsCorrectly() {
        ParameterContainer parameters = new ParameterContainer();
        parameters.addParameter("message", "the message you wish to send", false);

        CommandHelp expected = new CommandHelp("send-message-chat",
                "sends a message to all users in the chat you are currently in",
                parameters);

        CommandHelp actual = SendMessageInChatCommand.help();

        assertEquals(expected.toString(), actual.toString(),
                "Help method should correct information about the command.");
    }
}