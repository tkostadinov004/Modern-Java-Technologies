package bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator.Authenticator;
import bg.sofia.uni.fmi.mjt.splitwise.server.chat.ChatException;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.Command;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.ChatMessagesRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.ChatRepository;

import java.io.PrintWriter;

public class CreateChatCommand extends Command {
    private static final int ARGUMENTS_NEEDED = 0;
    private Authenticator authenticator;
    private ChatRepository chatRepository;
    private ChatMessagesRepository chatMessagesRepository;

    public CreateChatCommand(Authenticator authenticator, ChatRepository chatRepository, ChatMessagesRepository chatMessagesRepository, String[] args) {
        super(ARGUMENTS_NEEDED, args);
        this.authenticator = authenticator;
        this.chatRepository = chatRepository;
        this.chatMessagesRepository = chatMessagesRepository;
    }

    @Override
    public void execute(PrintWriter writer) {
        if (!authenticator.isAuthenticated()) {
            writer.println("You have to be logged in!");
            return;
        }

        try {
            String roomCode = chatRepository.createRoom(chatMessagesRepository);
            writer.println("Chat room created with code %s".formatted(roomCode));
            writer.println("You can enter this chat room by typing \"join-room <room-code>\"");
            writer.println("Send this code to your friends so that they can connect to your chat room!");
        } catch (ChatException e) {
            writer.println("Unexpected error in creating a chat room!");
        }
    }
}
