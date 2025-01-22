package bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts;

import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;

import java.util.Set;

public interface UserFriendsRepository {
    boolean isFriendOf(String firstUsername, String secondUsername);

    boolean isFriendOf(User first, User second);

    Set<User> getFriendsOf(String username);

    Set<User> getFriendsOf(User user);

    void makeFriends(String firstUsername, String secondUsername);

    void makeFriends(User first, User second);
}
