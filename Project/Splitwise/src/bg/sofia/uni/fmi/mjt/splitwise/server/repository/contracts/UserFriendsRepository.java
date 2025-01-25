package bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts;

import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;

import java.util.Set;

public interface UserFriendsRepository {
    boolean isFriendOf(String firstUsername, String secondUsername);

    Set<User> getFriendsOf(String username);

    void makeFriends(String firstUsername, String secondUsername);
}
