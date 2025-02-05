package bg.sofia.uni.fmi.mjt.splitwise.server.data.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.data.CsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.dto.FriendshipRelationDTO;
import com.opencsv.CSVReader;

import java.util.Set;
import java.util.stream.Collectors;

public class UserFriendsCsvProcessor extends CsvProcessor<FriendshipRelationDTO> {
    public UserFriendsCsvProcessor(CSVReader reader, String filePath) {
        super(reader, filePath);
    }

    private static final int FIRST_INDEX = 0;
    private static final int SECOND_INDEX = 1;

    private FriendshipRelationDTO parseRelation(String[] args) {
        return new FriendshipRelationDTO(args[FIRST_INDEX], args[SECOND_INDEX]);
    }

    @Override
    public Set<FriendshipRelationDTO> readAll() {
        Set<FriendshipRelationDTO> relations = super.readAll(this::parseRelation);
        relations.addAll(relations
                .stream()
                .map(relation -> new FriendshipRelationDTO(relation.secondUsername(), relation.firstUsername()))
                .collect(Collectors.toSet()));
        return relations;
    }

    private String serializeRelation(FriendshipRelationDTO relation) {
        return "\"%s\",\"%s\""
                .formatted(relation.firstUsername(), relation.secondUsername());
    }

    @Override
    public synchronized void writeToFile(FriendshipRelationDTO obj) {
        super.writeToFile(obj, this::serializeRelation);
    }
}
