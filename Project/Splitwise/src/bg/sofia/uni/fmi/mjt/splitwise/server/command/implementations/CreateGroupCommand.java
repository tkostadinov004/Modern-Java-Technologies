package bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator.Authenticator;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.VariableArgumentsCommand;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.CommandHelp;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.ParameterContainer;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.FriendGroupRepository;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class CreateGroupCommand extends VariableArgumentsCommand {
    private static final int ARGUMENTS_NEEDED = 3;
    private final Authenticator authenticator;
    private final FriendGroupRepository friendGroupRepository;

    private static final int GROUP_NAME_INDEX = 0;

    public CreateGroupCommand(Authenticator authenticator, FriendGroupRepository friendGroupRepository, String[] args) {
        super(ARGUMENTS_NEEDED, args);
        this.authenticator = authenticator;
        this.friendGroupRepository = friendGroupRepository;
    }

    @Override
    public boolean execute(PrintWriter writer) {
        if (!authenticator.isAuthenticated()) {
            writer.println("You have to be logged in!");
            return false;
        }

        Set<String> groupParticipants = Arrays.stream(arguments).skip(1).collect(Collectors.toSet());
        groupParticipants.add(authenticator.getAuthenticatedUser().username());

        try {
            friendGroupRepository.createGroup(arguments[GROUP_NAME_INDEX], groupParticipants);
            writer.println("Successfully created group %s.".formatted(arguments[GROUP_NAME_INDEX]));
            return true;
        } catch (RuntimeException e) {
            writer.println(e.getMessage());
            return false;
        }
    }

    public static CommandHelp help() {
        ParameterContainer parameters = new ParameterContainer();
        parameters.addParameter("group-name", "the name of the group you wish to create", false);
        parameters.addVariableParameter("user", "the username of a user you want included in the group",
                ARGUMENTS_NEEDED - 1, false);

        return new CommandHelp("create-group",
                "creates a group, consisting of you and the other users entered in the command",
                parameters);
    }
}
