package bg.sofia.uni.fmi.mjt.splitwise.server.models;

import java.util.Set;

public record FriendGroup(String name, Set<User> participants) {
    @Override
    public String toString() {
        return "%s: [%s]".formatted(name, String.join(", ",
                        participants.stream().map(User::toString).toList()));
    }
}
