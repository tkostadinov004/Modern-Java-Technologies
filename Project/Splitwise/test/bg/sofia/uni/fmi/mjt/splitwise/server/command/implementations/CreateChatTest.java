package bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator.Authenticator;
import bg.sofia.uni.fmi.mjt.splitwise.server.chat.exception.ChatException;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.Command;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.exception.CommandArgumentsCountException;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.CommandHelp;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.ParameterContainer;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.ChatRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.NonExistentUserException;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CreateChatTest {
    private static final PrintWriter printWriter = mock();

    @Test
    public void testDoesNotWorkWithInsufficientArguments() {
        Authenticator authenticator = mock();
        ChatRepository chatRepository = mock();

        assertThrows(CommandArgumentsCountException.class,
                () -> new CreateChatCommand(authenticator, chatRepository, new String[1]),
                "Exception should be thrown if there are more arguments than needed");
    }

    @Test
    public void testDoesNotWorkWhenUserIsNotAuthenticated() throws ChatException {
        Authenticator authenticator = mock();
        when(authenticator.isAuthenticated()).thenReturn(false);

        ChatRepository chatRepository = mock();

        Command command =
                new CreateChatCommand(authenticator, chatRepository, new String[0]);
        assertFalse(command.execute(printWriter),
                "Command should not work when user is not authenticated");

        verify(chatRepository, times(0))
                .createRoom();
    }

    @Test
    public void testWorksWhenUserIsAuthenticated() throws ChatException {
        Authenticator authenticator = mock();
        when(authenticator.getAuthenticatedUser())
                .thenReturn(new User("testuser", "pass", "fn", "ln"));
        when(authenticator.isAuthenticated()).thenReturn(true);

        ChatRepository chatRepository = mock();

        Command command =
                new CreateChatCommand(authenticator, chatRepository, new String[0]);

        assertTrue(command.execute(printWriter),
                "Command should work when user is authenticated");

        verify(chatRepository, times(1))
                .createRoom();
    }

    @Test
    public void testHandlesChatException() throws ChatException{
        Authenticator authenticator = mock();
        when(authenticator.getAuthenticatedUser())
                .thenReturn(new User("testuser", "pass", "fn", "ln"));
        when(authenticator.isAuthenticated()).thenReturn(true);

        ChatRepository chatRepository = mock();
        doThrow(ChatException.class)
                .when(chatRepository)
                .createRoom();
        Command command =
                new CreateChatCommand(authenticator, chatRepository, new String[0]);
        assertThrows(ChatException.class, () -> chatRepository.createRoom());
        assertFalse(command.execute(printWriter),
                "Command should handle server errors");
    }

    @Test
    public void testHelpReturnsCorrectly() {
        CommandHelp expected = new CommandHelp("create-chat",
                "creates a chat room and prints the chat code that you and " +
                        "your friends may use to join and discuss your expenses",
                new ParameterContainer());

        CommandHelp actual = CreateChatCommand.help();

        assertEquals(expected.toString(), actual.toString(),
                "Help method should correct information about the command.");
    }
}
