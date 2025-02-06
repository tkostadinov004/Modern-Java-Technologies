package bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.data.CsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.data.implementations.FriendGroupsCsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.dependency.DependencyContainer;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.FriendGroup;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.dto.FriendGroupDTO;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.FriendGroupRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.GroupAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.NonExistentUserException;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations.converter.DataConverter;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations.converter.FriendGroupConverter;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class DefaultFriendGroupRepository implements FriendGroupRepository {
    private final CsvProcessor<FriendGroupDTO> csvProcessor;
    private final UserRepository userRepository;
    private final Set<FriendGroup> friendGroups;

    public DefaultFriendGroupRepository(DependencyContainer dependencyContainer) {
        this.csvProcessor = dependencyContainer.get(FriendGroupsCsvProcessor.class);
        this.userRepository = dependencyContainer.get(UserRepository.class);

        DataConverter<Set<FriendGroup>, FriendGroup, FriendGroupDTO> converter =
                new FriendGroupConverter(csvProcessor, userRepository);
        this.friendGroups = converter.populate(Collectors.toSet());
    }

    @Override
    public Set<FriendGroup> getGroupsOf(String username) {
        if (username == null || username.isEmpty() || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null, blank or empty!");
        }

        Optional<User> user = userRepository.getUserByUsername(username);
        if (user.isEmpty()) {
            throw new NonExistentUserException("User with name %s does not exist!".formatted(username));
        }

        return friendGroups
                .stream()
                .filter(group -> group.participants().contains(user.get()))
                .collect(Collectors.toSet());
    }

    @Override
    public Optional<FriendGroup> getGroup(String groupName) {
        if (groupName == null || groupName.isEmpty() || groupName.isBlank()) {
            throw new IllegalArgumentException("Group name cannot be null, blank or empty!");
        }

        return friendGroups
                .stream()
                .filter(group -> group.name().equals(groupName))
                .findFirst();
    }

    @Override
    public boolean containsGroupByName(String groupName) {
        if (groupName == null || groupName.isEmpty() || groupName.isBlank()) {
            throw new IllegalArgumentException("Group name cannot be null, blank or empty!");
        }

        return getGroup(groupName).isPresent();
    }

    @Override
    public void createGroup(String groupName, Set<String> friendsUsernames) {
        if (groupName == null || groupName.isEmpty() || groupName.isBlank()) {
            throw new IllegalArgumentException("Group name cannot be null, blank or empty!");
        }
        if (friendsUsernames == null) {
            throw new IllegalArgumentException("Usernames set cannot be null!");
        }
        if (friendsUsernames.isEmpty()) {
            throw new IllegalArgumentException("You cannot create a friend group with no friends in it!");
        }
        if (containsGroupByName(groupName)) {
            throw new GroupAlreadyExistsException("Group with name %s already exists!".formatted(groupName));
        }
        Set<User> users = friendsUsernames.stream()
                .map(username -> {
                    Optional<User> user = userRepository.getUserByUsername(username);
                    if (user.isEmpty()) {
                        throw new NonExistentUserException("User with username %s does not exist!"
                                .formatted(username));
                    }
                    return user.get();
                }).collect(Collectors.toCollection(HashSet::new));

        FriendGroup group = new FriendGroup(groupName, users);
        friendGroups.add(group);
        csvProcessor.writeToFile(new FriendGroupDTO(groupName, friendsUsernames));
    }
}
