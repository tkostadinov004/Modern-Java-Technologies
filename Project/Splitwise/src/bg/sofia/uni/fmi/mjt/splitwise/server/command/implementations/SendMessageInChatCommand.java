package bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator.Authenticator;
import bg.sofia.uni.fmi.mjt.splitwise.server.chat.exception.ChatException;
import bg.sofia.uni.fmi.mjt.splitwise.server.chat.token.ChatToken;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.StandardCommand;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.CommandHelp;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.ParameterContainer;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.ChatRepository;

import java.io.PrintWriter;

public class SendMessageInChatCommand extends StandardCommand {
    private static final int ARGUMENTS_NEEDED = 1;
    private final Authenticator authenticator;
    private final ChatToken chatToken;
    private final ChatRepository chatRepository;

    public SendMessageInChatCommand(Authenticator authenticator,
                                    ChatToken chatToken,
                                    ChatRepository chatRepository,
                                    String[] args) {
        super(ARGUMENTS_NEEDED, args);
        this.authenticator = authenticator;
        this.chatToken = chatToken;
        this.chatRepository = chatRepository;
    }

    private static final int MESSAGE_INDEX = 0;

    @Override
    public boolean execute(PrintWriter writer) {
        if (!authenticator.isAuthenticated()) {
            writer.println("You have to be logged in!");
            return false;
        }
        if (!chatToken.isInChat()) {
            writer.println("You have to be in a chat in order to send a message!");
            return false;
        }

        try {
            chatRepository.sendMessage(authenticator.getAuthenticatedUser().username(),
                    chatToken.getServer().code(),
                    arguments[MESSAGE_INDEX]);
            return true;
        } catch (ChatException | IllegalArgumentException e) {
            writer.println(e.getMessage());
        }
        return false;
    }

    public static CommandHelp help() {
        ParameterContainer parameters = new ParameterContainer();
        parameters.addParameter("message", "the message you wish to send", false);

        return new CommandHelp("send-message-chat",
                "sends a message to all users in the chat you are currently in",
                parameters);
    }
}
