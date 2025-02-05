package bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.data.CsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.data.implementations.FriendGroupsCsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.dependency.DependencyContainer;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.FriendGroup;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.dto.FriendGroupDTO;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.FriendGroupRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.FriendGroupException;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.GroupAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.NonExistingGroupException;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.NonExistentUserException;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class DefaultFriendGroupRepository implements FriendGroupRepository {
    private final CsvProcessor<FriendGroupDTO> csvProcessor;
    private final UserRepository userRepository;
    private final Set<FriendGroup> friendGroups;

    private FriendGroup createFromDTO(FriendGroupDTO dto) {
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
            return new FriendGroup(dto.name(), participants);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private Set<FriendGroup> populateFriendGroups() {
        return csvProcessor
                .readAll()
                .stream()
                .map(this::createFromDTO)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public DefaultFriendGroupRepository(DependencyContainer dependencyContainer) {
        this.csvProcessor = dependencyContainer.get(FriendGroupsCsvProcessor.class);
        this.userRepository = dependencyContainer.get(UserRepository.class);
        this.friendGroups = populateFriendGroups();
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
    public boolean isInGroup(String username, String groupName) {
        if (username == null || username.isEmpty() || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null, blank or empty!");
        }
        if (groupName == null || groupName.isEmpty() || groupName.isBlank()) {
            throw new IllegalArgumentException("Group name cannot be null, blank or empty!");
        }

        Optional<User> user = userRepository.getUserByUsername(username);
        if (user.isEmpty()) {
            throw new NonExistentUserException("User with name %s does not exist!".formatted(username));
        }

        Optional<FriendGroup> group = getGroup(groupName);
        if (group.isEmpty()) {
            throw new NonExistingGroupException("Group with name %s does not exist!".formatted(groupName));
        }

        return group.get().participants().contains(user.get());
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

    @Override
    public void removeFromGroup(String username, String groupName) {
        if (username == null || username.isEmpty() || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null, blank or empty!");
        }
        if (groupName == null || groupName.isEmpty() || groupName.isBlank()) {
            throw new IllegalArgumentException("Group name cannot be null, blank or empty!");
        }

        Optional<User> user = userRepository.getUserByUsername(username);
        if (user.isEmpty()) {
            throw new NonExistentUserException("User with name %s does not exist!".formatted(username));
        }

        Optional<FriendGroup> group = getGroup(groupName);
        if (group.isEmpty()) {
            throw new NonExistingGroupException("Group with name %s does not exist!".formatted(groupName));
        }

        if (!group.get().participants().remove(user.get())) {
            throw new FriendGroupException("User with username %s is not in group with name %s!"
                    .formatted(user.get().username(), group.get().name()));
        }
        csvProcessor.modify(g -> g.name().equals(groupName),
                new FriendGroupDTO(groupName,
                        group.get().participants().stream().map(User::username).collect(Collectors.toSet())));
    }

    @Override
    public void removeGroup(String groupName) {
        if (groupName == null || groupName.isEmpty() || groupName.isBlank()) {
            throw new IllegalArgumentException("Group name cannot be null, blank or empty!");
        }

        Optional<FriendGroup> group = getGroup(groupName);
        if (group.isEmpty()) {
            throw new NonExistingGroupException("Group with name %s does not exist!".formatted(groupName));
        }

        if (!friendGroups.remove(group.get())) {
            throw new NonExistingGroupException("Group with name %s does not exist!".formatted(group.get().name()));
        }
        csvProcessor.remove(g -> g.name().equals(groupName));
    }
}
