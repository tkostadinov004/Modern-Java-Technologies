package bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations.converter;

import bg.sofia.uni.fmi.mjt.splitwise.server.data.CsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.FriendshipRelation;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.dto.FriendshipRelationDTO;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserRepository;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class UserFriendsConverter
        extends DataConverter<Map<User, Set<User>>, FriendshipRelation, FriendshipRelationDTO> {
    private final UserRepository userRepository;

    public UserFriendsConverter(CsvProcessor<FriendshipRelationDTO> csvProcessor, UserRepository userRepository) {
        super(csvProcessor);
        this.userRepository = userRepository;
    }

    @Override
    public FriendshipRelation createFromDTO(FriendshipRelationDTO dto) {
        Optional<User> first = userRepository.getUserByUsername(dto.firstUsername());
        if (first.isEmpty()) {
            return null;
        }
        Optional<User> second = userRepository.getUserByUsername(dto.secondUsername());
        if (second.isEmpty()) {
            return null;
        }
        return new FriendshipRelation(first.get(), second.get());
    }
}
