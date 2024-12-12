package bg.sofia.uni.fmi.mjt.socialnetwork.post;

import bg.sofia.uni.fmi.mjt.socialnetwork.profile.UserProfile;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

public class SocialFeedPost implements Post {
    private final String uniqueId;
    private final UserProfile author;
    private LocalDateTime publishedOn;
    private String content;
    private final Map<UserProfile, ReactionType> reactions;

    public SocialFeedPost(UserProfile author, String content) {
        uniqueId = UUID.randomUUID().toString();
        this.author = author;
        this.content = content;
        this.publishedOn = LocalDateTime.now();
        reactions = new LinkedHashMap<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SocialFeedPost that = (SocialFeedPost) o;
        return Objects.equals(uniqueId, that.uniqueId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uniqueId);
    }

    @Override
    public String getUniqueId() {
        return uniqueId;
    }

    @Override
    public UserProfile getAuthor() {
        return author;
    }

    @Override
    public LocalDateTime getPublishedOn() {
        return publishedOn;
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public boolean addReaction(UserProfile userProfile, ReactionType reactionType) {
        if (userProfile == null) {
            throw new IllegalArgumentException("User profile cannot be null!");
        }
        if (reactionType == null) {
            throw new IllegalArgumentException("Reaction type cannot be null!");
        }

        if (reactions.containsKey(userProfile)) {
            reactions.replace(userProfile, reactionType);
            return false;
        }

        reactions.put(userProfile, reactionType);
        return true;
    }

    @Override
    public boolean removeReaction(UserProfile userProfile) {
        if (userProfile == null) {
            throw new IllegalArgumentException("User profile cannot be null!");
        }
        return reactions.remove(userProfile) != null;
    }

    @Override
    public Map<ReactionType, Set<UserProfile>> getAllReactions() {
        Map<ReactionType, Set<UserProfile>> result = new EnumMap<>(ReactionType.class);
        for (var reaction : reactions.entrySet()) {
            result.putIfAbsent(reaction.getValue(), new TreeSet<>());
            result.get(reaction.getValue()).add(reaction.getKey());
        }
        return Collections.unmodifiableMap(result);
    }

    @Override
    public int getReactionCount(ReactionType reactionType) {
        if (reactionType == null) {
            throw new IllegalArgumentException("Reaction type cannot be null!");
        }
        Map<ReactionType, Set<UserProfile>> reactions = getAllReactions();
        if (!reactions.containsKey(reactionType)) {
            return 0;
        }
        return reactions.get(reactionType).size();
    }

    @Override
    public int totalReactionsCount() {
        return reactions.size();
    }
}
