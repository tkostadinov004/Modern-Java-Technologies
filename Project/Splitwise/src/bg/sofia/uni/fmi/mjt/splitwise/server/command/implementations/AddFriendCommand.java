package bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator.Authenticator;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.Command;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.StandardCommand;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.CommandHelp;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.ParameterContainer;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.ParameterHelp;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserFriendsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.AlreadyFriendsException;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class AddFriendCommand extends StandardCommand {
    private static final int ARGUMENTS_NEEDED = 1;
    private Authenticator authenticator;
    private UserFriendsRepository userFriendsRepository;

    private static final int USERNAME_INDEX = 0;

    public AddFriendCommand(Authenticator authenticator, UserFriendsRepository userFriendsRepository,String[] args) {
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

        try {
            userFriendsRepository.makeFriends(authenticator.getAuthenticatedUser().username(), arguments[USERNAME_INDEX]);
            writer.println("Successfully added %s as your friend.".formatted(arguments[USERNAME_INDEX]));
        } catch (AlreadyFriendsException e) {
            throw new RuntimeException("You are already friends with %s".formatted(arguments[USERNAME_INDEX]));
        }
    }

    public static CommandHelp help() {
        ParameterContainer parameters = new ParameterContainer();
        parameters.addParameter("username", "the username of the person you want to add as a friend", false);

        return new CommandHelp("add-friend",
                "adds the specified user as your friend, allowing you to split bills and chat with them",
                parameters);
    }
}
