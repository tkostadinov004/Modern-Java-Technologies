package bg.sofia.uni.fmi.mjt.splitwise.server.command.factory;

import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator.Authenticator;
import bg.sofia.uni.fmi.mjt.splitwise.server.chat.token.ChatToken;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.factory.parser.CommandParser;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations.AddFriendCommand;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations.ClearNotificationsCommand;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations.CreateChatCommand;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations.CreateGroupCommand;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations.ExitChatCommand;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations.ExportRecentGroupExpensesCommand;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations.ExportRecentPersonalExpensesCommand;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations.HelpCommand;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations.JoinChatCommand;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations.ListFriendsCommand;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations.ListGroupsCommand;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations.LoginCommand;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations.LogoutCommand;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations.PayedCommand;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations.PayedGroupCommand;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations.RegisterCommand;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations.SendMessageInChatCommand;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations.ShowNotificationsCommand;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations.SplitCommand;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations.SplitWithGroupCommand;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations.StatusCommand;
import bg.sofia.uni.fmi.mjt.splitwise.server.dependency.DependencyContainer;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CommandFactoryTest {
    private DependencyContainer dependencyContainer = mock();
    private Authenticator authenticator = mock();
    private ChatToken chatToken = mock();

    @Test
    public void testBuildThrowsOnInvalidInput() {
        CommandFactory factory = new CommandFactory(dependencyContainer, authenticator, chatToken);
        assertThrows(IllegalArgumentException.class, () -> factory.build("    ", new CommandParser()),
                "Exception should be thrown when presented with an invalid command input");
    }

    private Map<String, Class<?>> fillTypes() {
        Map<String, Class<?>> types = new HashMap<>();
        types.put("login" , LoginCommand.class);
        types.put("register" , RegisterCommand.class);
        types.put("logout" , LogoutCommand.class);
        types.put("help" , HelpCommand.class);
        types.put("add-friend" , AddFriendCommand.class);
        types.put("list-friends" , ListFriendsCommand.class);
        types.put("create-group" , CreateGroupCommand.class);
        types.put("list-groups" , ListGroupsCommand.class);
        types.put("split" , SplitCommand.class);
        types.put("split-group" , SplitWithGroupCommand.class);
        types.put("get-status" , StatusCommand.class);
        types.put("payed" , PayedCommand.class);
        types.put("payed-group" , PayedGroupCommand.class);
        types.put("show-notifications" , ShowNotificationsCommand.class);
        types.put("clear-notifications" , ClearNotificationsCommand.class);
        types.put("create-chat" , CreateChatCommand.class);
        types.put("join-chat" , JoinChatCommand.class);
        types.put("send-message-chat" , SendMessageInChatCommand.class);
        types.put("exit-chat" , ExitChatCommand.class);
        types.put("export-recent-personal-expenses" , ExportRecentPersonalExpensesCommand.class);
        types.put("export-recent-group-expenses" , ExportRecentGroupExpensesCommand.class);
        return types;
    }

    @Test
    public void testBuildCorrectly() {
        Map<String, Class<?>> types = fillTypes();
        CommandParser parser = mock();
        CommandFactory factory = new CommandFactory(dependencyContainer, authenticator, chatToken);

        types.forEach((commandName, commandType) -> {
            assertEquals(factory.build(commandName, parser).getClass(), commandType);
        });
    }
}
