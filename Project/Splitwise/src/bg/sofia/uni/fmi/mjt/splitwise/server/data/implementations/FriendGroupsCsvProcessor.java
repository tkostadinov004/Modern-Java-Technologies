package bg.sofia.uni.fmi.mjt.splitwise.server.data.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.data.CsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.FriendGroup;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserRepository;
import com.opencsv.CSVReader;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class FriendGroupsCsvProcessor extends CsvProcessor<FriendGroup> {
    private final UserRepository userRepository;

    public FriendGroupsCsvProcessor(UserRepository userRepository, CSVReader reader, String filePath) {
        super(reader, filePath);
        this.userRepository = userRepository;
    }

    private static final int NAME_INDEX = 0;
    private static final int PARTICIPANTS_INDEX = 1;

    private User parseUser(String username) {
        Optional<User> user = userRepository.getUserByUsername(username);
        if (user.isEmpty()) {
            throw new IllegalArgumentException();
        }
        return user.get();
    }

    private FriendGroup parseGroup(String[] args) {
        Set<User> groupParticipants;
        try {
            groupParticipants = Arrays.stream(args[PARTICIPANTS_INDEX]
                    .split(","))
                    .map(this::parseUser)
                    .collect(Collectors.toSet());
        } catch (IllegalArgumentException e) {
            return null;
        }

        return new FriendGroup(args[NAME_INDEX], groupParticipants);
    }

    @Override
    public Set<FriendGroup> readAll() {
        return super.readAll(this::parseGroup);
    }

    private String serializeGroup(FriendGroup group) {
        String participants = String.join(",", group
                .participants()
                .stream()
                .map(user -> user.username())
                .collect(Collectors.toSet()));

        return "\"%s\",\"%s\""
                .formatted(group.name(), participants);
    }

    @Override
    public synchronized void writeToFile(FriendGroup obj) {
        super.writeToFile(obj, this::serializeGroup);
    }
}
