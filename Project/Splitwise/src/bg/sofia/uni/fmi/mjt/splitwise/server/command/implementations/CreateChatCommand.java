package bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator.Authenticator;
import bg.sofia.uni.fmi.mjt.splitwise.server.chat.exception.ChatException;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.StandardCommand;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.CommandHelp;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.ParameterContainer;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.ChatRepository;

import java.io.PrintWriter;

public class CreateChatCommand extends StandardCommand {
    private static final int ARGUMENTS_NEEDED = 0;
    private final Authenticator authenticator;
    private final ChatRepository chatRepository;

    public CreateChatCommand(Authenticator authenticator, ChatRepository chatRepository, String[] args) {
        super(ARGUMENTS_NEEDED, args);
        this.authenticator = authenticator;
        this.chatRepository = chatRepository;
    }

    @Override
    public boolean execute(PrintWriter writer) {
        if (!authenticator.isAuthenticated()) {
            writer.println("You have to be logged in!");
            return false;
        }

        try {
            String roomCode = chatRepository.createRoom();
            writer.println("Chat room created with code %s".formatted(roomCode));
            writer.println("You can enter this chat room by typing \"join-chat <room-code>\"");
            writer.println("Send this code to your friends so that they can connect to your chat room!");
            return true;
        } catch (ChatException e) {
            writer.println("Unexpected error in creating a chat room!");
            return false;
        }
    }

    public static CommandHelp help() {
        return new CommandHelp("create-chat",
                "creates a chat room and prints the chat code that you and " +
                        "your friends may use to join and discuss your expenses",
                new ParameterContainer());
    }
}
