package bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator.Authenticator;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.Command;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.FriendGroupRepository;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class CreateGroupCommand extends Command {
    private static final int ARGUMENTS_NEEDED = 3;
    private Authenticator authenticator;
    private FriendGroupRepository friendGroupRepository;

    private static final int GROUP_NAME_INDEX = 0;

    public CreateGroupCommand(Authenticator authenticator, FriendGroupRepository friendGroupRepository, String[] args) {
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

        Set<String> groupParticipants = Arrays.stream(arguments).skip(1).collect(Collectors.toSet());
        groupParticipants.add(authenticator.getAuthenticatedUser().username());

        friendGroupRepository.createGroup(arguments[GROUP_NAME_INDEX], groupParticipants);
        writer.println("Successfully created group %s.".formatted(arguments[GROUP_NAME_INDEX]));
    }
}
