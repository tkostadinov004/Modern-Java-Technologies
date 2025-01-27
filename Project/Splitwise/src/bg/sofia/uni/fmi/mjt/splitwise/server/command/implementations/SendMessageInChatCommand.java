package bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator.Authenticator;
import bg.sofia.uni.fmi.mjt.splitwise.server.chat.ChatException;
import bg.sofia.uni.fmi.mjt.splitwise.server.chat.ChatToken;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.Command;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.ChatRepository;

import java.io.IOException;
import java.io.PrintWriter;

public class SendMessageInChatCommand extends Command {
    private static final int ARGUMENTS_NEEDED = 1;
    private Authenticator authenticator;
    private ChatToken chatToken;
    private ChatRepository chatRepository;

    public SendMessageInChatCommand(Authenticator authenticator, ChatToken chatToken, ChatRepository chatRepository, String[] args) {
        super(ARGUMENTS_NEEDED, args);
        this.authenticator = authenticator;
        this.chatToken = chatToken;
        this.chatRepository = chatRepository;
    }

    private static final int MESSAGE_INDEX = 0;

    @Override
    public void execute(PrintWriter writer) {
        if (!authenticator.isAuthenticated()) {
            writer.println("You have to be logged in!");
            return;
        }
        if (!chatToken.isInChat()) {
            writer.println("You have to be in a chat in order to send a message!");
            return;
        }

        try {
            chatRepository.sendMessage(authenticator.getAuthenticatedUser().username(), chatToken.getServer().code(), arguments[MESSAGE_INDEX]);
        } catch (ChatException e) {
            writer.println(e.getMessage());
        }
    }
}
