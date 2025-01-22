package bg.sofia.uni.fmi.mjt.splitwise.server.models;

import java.util.Set;

public record FriendGroup(String name, Set<User> participants) {
}
