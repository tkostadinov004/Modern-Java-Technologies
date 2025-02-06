package bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator.Authenticator;
import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.exception.NotAuthenticatedException;
import bg.sofia.uni.fmi.mjt.splitwise.server.chat.exception.ChatException;
import bg.sofia.uni.fmi.mjt.splitwise.server.chat.token.ChatToken;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.StandardCommand;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.CommandHelp;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.ParameterContainer;

import java.io.PrintWriter;

public class JoinChatCommand extends StandardCommand {
    private static final int ARGUMENTS_NEEDED = 1;
    private final Authenticator authenticator;
    private final ChatToken chatToken;

    public JoinChatCommand(Authenticator authenticator,
                           ChatToken chatToken,
                           String[] args) {
        super(ARGUMENTS_NEEDED, args);
        this.authenticator = authenticator;
        this.chatToken = chatToken;
    }

    private static final int CHAT_CODE_INDEX = 0;

    @Override
    public void execute(PrintWriter writer) {
        if (!authenticator.isAuthenticated()) {
            writer.println("You have to be logged in!");
            return;
        }
        if (chatToken.isInChat()) {
            writer.println("You are already connected to a chat! Leave it before you join a new one!");
            return;
        }

        try {
            chatToken.joinChat(arguments[CHAT_CODE_INDEX]);
            writer.println("Successfully joined chat!");
        } catch (ChatException e) {
            writer.println(e.getMessage());
        } catch (NotAuthenticatedException e) {
            writer.println("You have to be logged in!");
        }
    }

    public static CommandHelp help() {
        ParameterContainer parameters = new ParameterContainer();
        parameters.addParameter("chat-code", "the code of the chat room you wish to join", false);

        return new CommandHelp("join-chat",
                "joins a given chat room, identified by the provided unique chat room code",
                parameters);
    }
}
