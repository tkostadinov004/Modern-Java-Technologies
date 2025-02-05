package bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.data.CsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.data.implementations.UserFriendsCsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.dependency.DependencyContainer;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.FriendshipRelation;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.dto.FriendshipRelationDTO;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserFriendsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.AlreadyFriendsException;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.NonExistentUserException;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class DefaultUserFriendsRepository implements UserFriendsRepository {
    private final CsvProcessor<FriendshipRelationDTO> csvProcessor;
    private final UserRepository userRepository;
    private final Map<User, Set<User>> friendMap; 

    private FriendshipRelation createFromDTO(FriendshipRelationDTO dto) {
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

    private Map<User, Set<User>> populateFriendMap() {
        return csvProcessor
                .readAll()
                .stream()
                .map(this::createFromDTO)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(rel -> rel.first(),
                        Collectors.mapping(rel -> rel.second(), Collectors.toSet())));
    }

    public DefaultUserFriendsRepository(DependencyContainer dependencyContainer) {
        this.csvProcessor = dependencyContainer.get(UserFriendsCsvProcessor.class);
        this.userRepository = dependencyContainer.get(UserRepository.class);
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
            throw new NonExistentUserException("User with username %s does not exist!".formatted(firstUsername));
        }
        if (second.isEmpty()) {
            throw new NonExistentUserException("User with username %s does not exist!".formatted(secondUsername));
        }

        return friendMap.containsKey(first.get()) && friendMap.get(first.get()).contains(second.get());
    }

    @Override
    public Set<User> getFriendsOf(String username) {
        if (username == null || username.isEmpty() || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null, blank or empty!");
        }

        Optional<User> user = userRepository.getUserByUsername(username);
        if (user.isEmpty()) {
            throw new NonExistentUserException("User with username %s does not exist!".formatted(username));
        }

        if (!friendMap.containsKey(user.get())) {
            return Set.of();
        }
        return friendMap.get(user.get());
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
            throw new NonExistentUserException("User with username %s does not exist!".formatted(firstUsername));
        }
        if (second.isEmpty()) {
            throw new NonExistentUserException("User with username %s does not exist!".formatted(secondUsername));
        }

        friendMap.putIfAbsent(first.get(), new HashSet<>());
        friendMap.putIfAbsent(second.get(), new HashSet<>());
        if (friendMap.get(first.get()).contains(second.get())) {
            throw new AlreadyFriendsException("User %s is already friends with %s!"
                    .formatted(firstUsername, secondUsername));
        }
        friendMap.get(first.get()).add(second.get());
        friendMap.get(second.get()).add(first.get());
        csvProcessor.writeToFile(new FriendshipRelationDTO(firstUsername, secondUsername));
    }
}
