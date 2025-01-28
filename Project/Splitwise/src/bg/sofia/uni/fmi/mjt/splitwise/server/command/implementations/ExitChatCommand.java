package bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator.Authenticator;
import bg.sofia.uni.fmi.mjt.splitwise.server.chat.exception.ChatException;
import bg.sofia.uni.fmi.mjt.splitwise.server.chat.token.ChatToken;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.Command;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.StandardCommand;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.CommandHelp;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.ParameterContainer;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.ChatRepository;

import java.io.PrintWriter;

public class ExitChatCommand extends StandardCommand {
    private static final int ARGUMENTS_NEEDED = 0;
    private Authenticator authenticator;
    private ChatToken chatToken;
    private ChatRepository chatRepository;

    public ExitChatCommand(Authenticator authenticator, ChatToken chatToken, ChatRepository chatRepository, String[] args) {
        super(ARGUMENTS_NEEDED, args);
        this.authenticator = authenticator;
        this.chatToken = chatToken;
        this.chatRepository = chatRepository;
    }

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
            chatToken.leaveChat();
        } catch (ChatException e) {
            writer.println(e.getMessage());
        }
    }

    public static CommandHelp help() {
        return new CommandHelp("exit-chat",
                "leaves the chat room you are in. you can later rejoin by using the \"join-room\" " +
                        "command and the room code",
                new ParameterContainer());
    }
}
