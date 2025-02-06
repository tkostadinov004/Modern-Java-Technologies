package bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator.Authenticator;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.StandardCommand;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.CommandHelp;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.ParameterContainer;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserFriendsRepository;

import java.io.PrintWriter;
import java.util.List;

public class ListFriendsCommand extends StandardCommand {
    private static final int ARGUMENTS_NEEDED = 0;
    private final Authenticator authenticator;
    private final UserFriendsRepository userFriendsRepository;

    public ListFriendsCommand(Authenticator authenticator, UserFriendsRepository userFriendsRepository, String[] args) {
        super(ARGUMENTS_NEEDED, args);
        this.authenticator = authenticator;
        this.userFriendsRepository = userFriendsRepository;
    }

    @Override
    public void execute(PrintWriter writer) {
        if (!authenticator.isAuthenticated()) {
            writer.println("You have to be logged in!");
            return;
        }

        List<String> usernames = userFriendsRepository
                        .getFriendsOf(authenticator.getAuthenticatedUser().username())
                        .stream()
                        .map(u -> "%s (%s %s)".formatted(u.username(), u.firstName(), u.lastName()))
                        .toList();

        writer.println(String.join(", ", usernames));
    }

    public static CommandHelp help() {
        return new CommandHelp("list-friends",
                "lists all friends you have in your friend list",
                new ParameterContainer());
    }
}
