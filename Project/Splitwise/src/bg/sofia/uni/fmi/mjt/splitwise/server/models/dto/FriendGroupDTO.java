package bg.sofia.uni.fmi.mjt.splitwise.server.models.dto;

import java.util.Set;

public record FriendGroupDTO(String name, Set<String> participantsUsernames) {
}
