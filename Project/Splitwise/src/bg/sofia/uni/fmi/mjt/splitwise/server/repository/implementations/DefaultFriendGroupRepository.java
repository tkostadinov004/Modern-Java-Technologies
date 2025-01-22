package bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.models.FriendGroup;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.FriendGroupRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserFriendsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.FriendGroupException;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.GroupAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.NonExistingGroupException;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.NonExistingUserException;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class DefaultFriendGroupRepository implements FriendGroupRepository {
    private final UserRepository userRepository;
    private final UserFriendsRepository userFriendsRepository;
    private final Set<FriendGroup> friendGroups;

    public DefaultFriendGroupRepository(UserRepository userRepository, UserFriendsRepository userFriendsRepository) {
        this.userRepository = userRepository;
        this.userFriendsRepository = userFriendsRepository;
        this.friendGroups = new HashSet<>();
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
            throw new NonExistingUserException("User with name %s does not exist!".formatted(username));
        }

        Optional<FriendGroup> group = getGroup(groupName);
        if (group.isEmpty()) {
            throw new NonExistingGroupException("Group with name %s does not exist!".formatted(groupName));
        }

        return isInGroup(user.get(), group.get());
    }

    @Override
    public boolean isInGroup(User user, FriendGroup group) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null!");
        }
        if (group == null) {
            throw new IllegalArgumentException("Friend group cannot be null!");
        }

        return group.participants().contains(group);
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
        Set<String> nonExistingUsers = new HashSet<>();
        Set<User> users = friendsUsernames.stream()
                .map(username -> {
                    Optional<User> user = userRepository.getUserByUsername(username);
                    if (user.isEmpty()) {
                        nonExistingUsers.add(username);
                    }
                    return user.get();
                }).collect(Collectors.toCollection(HashSet::new));

        FriendGroup group = new FriendGroup(groupName, users);
        friendGroups.add(group);
        if (!nonExistingUsers.isEmpty()) {
            throw new NonExistingUserException("Users with usernames %s do not exist, therefore they're not included in the group"
                            .formatted(String.join(", ", nonExistingUsers)));
        }
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
            throw new NonExistingUserException("User with name %s does not exist!".formatted(username));
        }

        Optional<FriendGroup> group = getGroup(groupName);
        if (group.isEmpty()) {
            throw new NonExistingGroupException("Group with name %s does not exist!".formatted(groupName));
        }

        removeFromGroup(user.get(), group.get());
    }

    @Override
    public void removeFromGroup(User user, FriendGroup group) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null!");
        }
        if (group == null) {
            throw new IllegalArgumentException("Group cannot be null!");
        }

        boolean removedUser = group.participants().remove(user);
        if (!removedUser) {
            throw new FriendGroupException("User with username %s is not in group with name %s!"
                    .formatted(user.username(), group.name()));
        }
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

        removeGroup(group.get());
    }

    @Override
    public void removeGroup(FriendGroup group) {
        if (group == null) {
            throw new IllegalArgumentException("Group cannot be null!");
        }

        boolean removedGroup = friendGroups.remove(group);
        if (!removedGroup) {
            throw new NonExistingGroupException("Group with name %s does not exist!".formatted(group.name()));
        }
    }
}
