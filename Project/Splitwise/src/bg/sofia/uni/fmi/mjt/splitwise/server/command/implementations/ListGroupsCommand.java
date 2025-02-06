package bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator.Authenticator;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.StandardCommand;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.CommandHelp;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.ParameterContainer;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.FriendGroupRepository;

import java.io.PrintWriter;
import java.util.List;

public class ListGroupsCommand extends StandardCommand {
    private static final int ARGUMENTS_NEEDED = 0;
    private final Authenticator authenticator;
    private final FriendGroupRepository friendGroupRepository;

    public ListGroupsCommand(Authenticator authenticator, FriendGroupRepository friendGroupRepository, String[] args) {
        super(ARGUMENTS_NEEDED, args);
        this.authenticator = authenticator;
        this.friendGroupRepository = friendGroupRepository;
    }

    @Override
    public void execute(PrintWriter writer) {
        if (!authenticator.isAuthenticated()) {
            writer.println("You have to be logged in!");
            return;
        }

        List<String> groups = friendGroupRepository
                .getGroupsOf(authenticator.getAuthenticatedUser().username())
                .stream()
                .map(group -> "%s: [%s]".formatted(group.name(),
                        String.join(", ", group.participants().stream().map(User::username).toList())))
                .toList();

        groups.forEach(writer::println);
    }

    public static CommandHelp help() {
        return new CommandHelp("list-groups",
                "lists all groups you are part of",
                new ParameterContainer());
    }
}
