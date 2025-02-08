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
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations.converter.DataConverter;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations.converter.UserFriendsConverter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DefaultUserFriendsRepository implements UserFriendsRepository {
    private final CsvProcessor<FriendshipRelationDTO> csvProcessor;
    private final UserRepository userRepository;
    private final Map<User, Set<User>> friendMap;

    public DefaultUserFriendsRepository(DependencyContainer dependencyContainer) {
        this.csvProcessor = dependencyContainer.get(UserFriendsCsvProcessor.class);
        this.userRepository = dependencyContainer.get(UserRepository.class);

        DataConverter<Map<User, Set<User>>, FriendshipRelation, FriendshipRelationDTO> converter =
                new UserFriendsConverter(csvProcessor, userRepository);
        this.friendMap = new ConcurrentHashMap<>(converter.populate(Collectors.groupingBy(FriendshipRelation::first,
                Collectors.mapping(FriendshipRelation::second,
                        Collectors.toCollection(() -> Collections.synchronizedSet(new HashSet<>()))))));
    }

    @Override
    public boolean areFriends(String firstUsername, String secondUsername) {
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

        synchronized (friendMap) {
            return friendMap.containsKey(first.get()) && friendMap.get(first.get()).contains(second.get());
        }
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

        synchronized (friendMap) {
            if (!friendMap.containsKey(user.get())) {
                return Set.of();
            }
            return new HashSet<>(friendMap.get(user.get()));
        }
    }

    private void makeFriends(User first, User second) {
        friendMap.putIfAbsent(first, Collections.synchronizedSet(new HashSet<>()));
        friendMap.putIfAbsent(second, Collections.synchronizedSet(new HashSet<>()));
        synchronized (friendMap) {
            if (friendMap.get(first).contains(second)) {
                throw new AlreadyFriendsException("User %s is already friends with %s!"
                        .formatted(first.username(), second.username()));
            }
        }
        friendMap.get(first).add(second);
        friendMap.get(second).add(first);
        csvProcessor.writeToFile(new FriendshipRelationDTO(first.username(), second.username()));
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
        if (first.isEmpty()) {
            throw new NonExistentUserException("User with username %s does not exist!".formatted(firstUsername));
        }
        Optional<User> second = userRepository.getUserByUsername(secondUsername);
        if (second.isEmpty()) {
            throw new NonExistentUserException("User with username %s does not exist!".formatted(secondUsername));
        }

        makeFriends(first.get(), second.get());
    }
}
