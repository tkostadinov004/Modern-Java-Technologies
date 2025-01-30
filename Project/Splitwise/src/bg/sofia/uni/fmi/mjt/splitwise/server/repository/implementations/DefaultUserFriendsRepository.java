package bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.data.CsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.dto.FriendshipRelationDTO;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserFriendsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.AlreadyFriendsException;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.NonExistingUserException;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class DefaultUserFriendsRepository implements UserFriendsRepository {
    private final CsvProcessor<FriendshipRelationDTO> csvProcessor;
    private final UserRepository userRepository;
    private final Map<User, Set<User>> friendMap; 

    private Map<User, Set<User>> populateFriendMap() {
        Set<FriendshipRelationDTO> relations = csvProcessor.readAll();
        return relations
                .stream()
                .collect(Collectors.groupingBy(rel -> rel.first(), Collectors.mapping(rel -> rel.second(), Collectors.toSet())));
    }

    public DefaultUserFriendsRepository(CsvProcessor<FriendshipRelationDTO> csvProcessor, UserRepository userRepository) {
        this.csvProcessor = csvProcessor;
        this.userRepository = userRepository;
        this.friendMap = populateFriendMap();
    }

    @Override
    public boolean isFriendOf(String firstUsername, String secondUsername) {
        if (firstUsername == null || firstUsername.isEmpty() || firstUsername.isBlank()) {
            throw new IllegalArgumentException("First username cannot be null, blank or empty!");
        }
        if (secondUsername == null || secondUsername.isEmpty() || secondUsername.isBlank()) {
            throw new IllegalArgumentException("Second username cannot be null, blank or empty!");
        }

        Optional<User> first = userRepository.getUserByUsername(firstUsername);
        Optional<User> second = userRepository.getUserByUsername(secondUsername);
        if (first.isEmpty()) {
            throw new NonExistingUserException("User with username %s does not exist!".formatted(firstUsername));
        }
        if (second.isEmpty()) {
            throw new NonExistingUserException("User with username %s does not exist!".formatted(secondUsername));
        }

        return friendMap.containsKey(first) && friendMap.get(first).contains(second);
    }

    @Override
    public Set<User> getFriendsOf(String username) {
        if (username == null || username.isEmpty() || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null, blank or empty!");
        }

        Optional<User> user = userRepository.getUserByUsername(username);
        if (user.isEmpty()) {
            throw new NonExistingUserException("User with username %s does not exist!".formatted(username));
        }

        if (!friendMap.containsKey(user)) {
            return Set.of();
        }
        return friendMap.get(user);
    }

    @Override
    public void makeFriends(String firstUsername, String secondUsername) {
        if (firstUsername == null || firstUsername.isEmpty() || firstUsername.isBlank()) {
            throw new IllegalArgumentException("First username cannot be null, blank or empty!");
        }
        if (secondUsername == null || secondUsername.isEmpty() || secondUsername.isBlank()) {
            throw new IllegalArgumentException("Second username cannot be null, blank or empty!");
        }
        if (firstUsername.equals(secondUsername)) {
            throw new IllegalArgumentException("You cannot befriend yourself!");
        }

        Optional<User> first = userRepository.getUserByUsername(firstUsername);
        Optional<User> second = userRepository.getUserByUsername(secondUsername);
        if (first.isEmpty()) {
            throw new NonExistingUserException("User with username %s does not exist!".formatted(firstUsername));
        }
        if (second.isEmpty()) {
            throw new NonExistingUserException("User with username %s does not exist!".formatted(secondUsername));
        }

        friendMap.putIfAbsent(first.get(), new HashSet<>());
        friendMap.putIfAbsent(second.get(), new HashSet<>());

        if (friendMap.get(first.get()).contains(second.get())) {
            throw new AlreadyFriendsException("User %s is already friends with %s!"
                    .formatted(firstUsername, secondUsername));
        }

        friendMap.get(first.get()).add(second.get());
        friendMap.get(second.get()).add(first.get());
        csvProcessor.writeToFile(new FriendshipRelationDTO(first.get(), second.get()));
    }
}
