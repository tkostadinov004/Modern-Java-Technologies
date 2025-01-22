package bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts;

import bg.sofia.uni.fmi.mjt.splitwise.server.models.FriendGroup;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;

import java.util.Optional;
import java.util.Set;

public interface FriendGroupRepository {
    Optional<FriendGroup> getGroup(String groupName);

    boolean containsGroupByName(String groupName);

    boolean isInGroup(String username, String groupName);

    boolean isInGroup(User user, FriendGroup group);

    void createGroup(String groupName, Set<String> friendsUsernames);

    void removeFromGroup(String username, String groupName);

    void removeFromGroup(User user, FriendGroup group);

    void removeGroup(String groupName);

    void removeGroup(FriendGroup group);
}
