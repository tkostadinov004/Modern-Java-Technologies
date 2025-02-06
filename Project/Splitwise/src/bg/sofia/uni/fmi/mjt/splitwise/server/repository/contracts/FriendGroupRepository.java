package bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts;

import bg.sofia.uni.fmi.mjt.splitwise.server.models.FriendGroup;

import java.util.Optional;
import java.util.Set;

public interface FriendGroupRepository {
    Set<FriendGroup> getGroupsOf(String username);

    Optional<FriendGroup> getGroup(String groupName);

    boolean containsGroupByName(String groupName);

    void createGroup(String groupName, Set<String> friendsUsernames);
}
