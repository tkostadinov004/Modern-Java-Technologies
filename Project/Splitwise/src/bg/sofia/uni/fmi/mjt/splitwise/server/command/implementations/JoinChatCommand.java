package bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator.Authenticator;
import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.exception.NotAuthenticatedException;
import bg.sofia.uni.fmi.mjt.splitwise.server.chat.ChatException;
import bg.sofia.uni.fmi.mjt.splitwise.server.chat.ChatToken;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.Command;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.ChatRepository;

import java.io.PrintWriter;

public class JoinChatCommand extends Command {
    private static final int ARGUMENTS_NEEDED = 1;
    private Authenticator authenticator;
    private ChatToken chatToken;
    private ChatRepository chatRepository;

    public JoinChatCommand(Authenticator authenticator, ChatToken chatToken, ChatRepository chatRepository, String[] args) {
        super(ARGUMENTS_NEEDED, args);
        this.authenticator = authenticator;
        this.chatToken = chatToken;
        this.chatRepository = chatRepository;
    }

    private static final int CHAT_CODE_INDEX = 0;

    @Override
    public void execute(PrintWriter writer) {
        if (chatToken.isInChat()) {
            writer.println("You are already connected to a chat! Leave it before you join a new one!");
            return;
        }

        try {
            chatToken.joinChat(arguments[CHAT_CODE_INDEX]);
        } catch (ChatException e) {
            writer.println(e.getMessage());
        } catch (NotAuthenticatedException e) {
            writer.println("You have to be logged in!");
        }
    }
}
