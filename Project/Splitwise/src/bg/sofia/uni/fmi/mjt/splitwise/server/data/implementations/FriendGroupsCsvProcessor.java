package bg.sofia.uni.fmi.mjt.splitwise.server.data.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.data.CsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.dto.FriendGroupDTO;
import com.opencsv.CSVReader;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class FriendGroupsCsvProcessor extends CsvProcessor<FriendGroupDTO> {
    public FriendGroupsCsvProcessor(CSVReader reader, String filePath) {
        super(reader, filePath);
    }

    private static final int NAME_INDEX = 0;
    private static final int PARTICIPANTS_INDEX = 1;

    private FriendGroupDTO parseGroup(String[] args) {
        Set<String> groupParticipantsUsernames = Arrays.stream(args[PARTICIPANTS_INDEX]
                    .split(","))
                    .collect(Collectors.toSet());

        return new FriendGroupDTO(args[NAME_INDEX], groupParticipantsUsernames);
    }

    @Override
    public Set<FriendGroupDTO> readAll() {
        return super.readAll(this::parseGroup);
    }

    private String serializeGroup(FriendGroupDTO group) {
        String participants = String.join(",", new HashSet<>(group
                .participantsUsernames()));

        return "\"%s\",\"%s\""
                .formatted(group.name(), participants);
    }

    @Override
    public synchronized void writeToFile(FriendGroupDTO obj) {
        super.writeToFile(obj, this::serializeGroup);
    }
}
