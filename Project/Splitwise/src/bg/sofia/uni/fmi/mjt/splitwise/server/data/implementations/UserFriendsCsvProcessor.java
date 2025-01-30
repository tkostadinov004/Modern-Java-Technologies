package bg.sofia.uni.fmi.mjt.splitwise.server.data.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.data.CsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.dto.FriendshipRelationDTO;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserRepository;
import com.opencsv.CSVReader;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class UserFriendsCsvProcessor extends CsvProcessor<FriendshipRelationDTO> {
    private final UserRepository userRepository;

    public UserFriendsCsvProcessor(UserRepository userRepository, CSVReader reader, String filePath) {
        super(reader, filePath);
        this.userRepository = userRepository;
    }

    private static final int FIRST_INDEX = 0;
    private static final int SECOND_INDEX = 1;

    private FriendshipRelationDTO parseRelation(String[] args) {
        Optional<User> first = userRepository.getUserByUsername(args[FIRST_INDEX]);
        if (first.isEmpty()) {
            return null;
        }

        Optional<User> second = userRepository.getUserByUsername(args[SECOND_INDEX]);
        if (second.isEmpty()) {
            return null;
        }

        return new FriendshipRelationDTO(first.get(), second.get());
    }

    @Override
    public Set<FriendshipRelationDTO> readAll() {
        Set<FriendshipRelationDTO> relations = super.readAll(this::parseRelation);
        relations.addAll(relations
                .stream()
                .map(relation -> new FriendshipRelationDTO(relation.second(), relation.first()))
                .collect(Collectors.toSet()));
        return relations;
    }

    private String serializeRelation(FriendshipRelationDTO relation) {
        return "\"%s\",\"%s\""
                .formatted(relation.first().username(), relation.second().username());
    }

    @Override
    public synchronized void writeToFile(FriendshipRelationDTO obj) {
        super.writeToFile(obj, this::serializeRelation);
    }
}
