package bg.sofia.uni.fmi.mjt.socialnetwork.profile;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class DefaultUserProfile implements Comparable<DefaultUserProfile>, UserProfile {
    private final String username;
    private final Set<Interest> interests;
    private final Set<UserProfile> friends;
    public DefaultUserProfile(String username) {
        this.username = username;
        interests = EnumSet.noneOf(Interest.class);
        friends = new LinkedHashSet<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultUserProfile that = (DefaultUserProfile) o;
        return Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(username);
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public Collection<Interest> getInterests() {
        return Collections.unmodifiableSet(interests);
    }

    @Override
    public boolean addInterest(Interest interest) {
        if (interest == null) {
            throw new IllegalArgumentException("Invalid interest!");
        }
        return interests.add(interest);
    }

    @Override
    public boolean removeInterest(Interest interest) {
        if (interest == null) {
            throw new IllegalArgumentException("Invalid interest!");
        }
        return interests.remove(interest);
    }

    @Override
    public Collection<UserProfile> getFriends() {
        return Collections.unmodifiableSet(friends);
    }

    @Override
    public boolean addFriend(UserProfile userProfile) {
        if (userProfile == null) {
            throw new IllegalArgumentException("User profile cannot be null!");
        }
        if (this == userProfile) {
            throw new IllegalArgumentException("User profile should be different than the current one!");
        }

        boolean isAdded = friends.add(userProfile);
        if (!userProfile.isFriend(this)) {
            return userProfile.addFriend(this);
        }
        return isAdded;
    }

    @Override
    public boolean unfriend(UserProfile userProfile) {
        if (userProfile == null) {
            throw new IllegalArgumentException("User profile cannot be null!");
        }
        boolean isRemoved = friends.remove(userProfile);
        if (userProfile.isFriend(this)) {
            return userProfile.unfriend(this);
        }
        return isRemoved;
    }

    @Override
    public boolean isFriend(UserProfile userProfile) {
        if (userProfile == null) {
            throw new IllegalArgumentException("User profile cannot be null!");
        }
        return friends.contains(userProfile);
    }

    @Override
    public int compareTo(DefaultUserProfile o) {
        return this.username.compareTo(o.username);
    }
}