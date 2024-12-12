package bg.sofia.uni.fmi.mjt.socialnetwork.profile.filter;

import bg.sofia.uni.fmi.mjt.socialnetwork.profile.Interest;
import bg.sofia.uni.fmi.mjt.socialnetwork.profile.UserProfile;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class UserByFriendNetworkFilter implements Filter {
    private boolean areInterestsOverlapping(UserProfile user1, UserProfile user2) {
        if (user1 == null || user2 == null) {
            throw new IllegalArgumentException("User profile cannot be null!");
        }
        Set<Interest> commonInterests = new HashSet<>(user1.getInterests());
        commonInterests.retainAll(user2.getInterests());
        return !commonInterests.isEmpty();
    }

    @Override
    public Set<UserProfile> getMatches(UserProfile user) {
        if (user == null) {
            throw new IllegalArgumentException("User profile cannot be null!");
        }

        Queue<UserProfile> profiles = new LinkedList<>();
        Set<UserProfile> visited = new HashSet<>();
        Set<UserProfile> validUsers = new HashSet<>();

        visited.add(user);
        for (var friend : user.getFriends()) {
            profiles.add(friend);
            visited.add(friend);
        }

        while (!profiles.isEmpty()) {
            UserProfile currentProfile = profiles.poll();
            if (areInterestsOverlapping(user, currentProfile)) {
                validUsers.add(currentProfile);
            }

            for (var friend : currentProfile.getFriends()) {
                if (!visited.contains(friend)) {
                    profiles.add(friend);
                    visited.add(friend);
                }
            }
        }
        return validUsers;
    }
}