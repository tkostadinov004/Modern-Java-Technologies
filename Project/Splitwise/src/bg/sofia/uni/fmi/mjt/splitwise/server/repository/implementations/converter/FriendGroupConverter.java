package bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations.converter;

import bg.sofia.uni.fmi.mjt.splitwise.server.data.CsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.FriendGroup;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.dto.FriendGroupDTO;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserRepository;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class FriendGroupConverter
        extends DataConverter<Set<FriendGroup>, FriendGroup, FriendGroupDTO> {
    private final UserRepository userRepository;

    public FriendGroupConverter(CsvProcessor<FriendGroupDTO> csvProcessor, UserRepository userRepository) {
        super(csvProcessor);
        this.userRepository = userRepository;
    }

    @Override
    public FriendGroup createFromDTO(FriendGroupDTO dto) {
        try {
            Set<User> participants = dto
                    .participantsUsernames()
                    .stream().map(username -> {
                        Optional<User> user = userRepository.getUserByUsername(username);
                        if (user.isEmpty()) {
                            throw new IllegalArgumentException();
                        }
                        return user.get();
                    }).collect(Collectors.toSet());
            if (participants.isEmpty()) {
                return null;
            }
            return new FriendGroup(dto.name(), participants);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
