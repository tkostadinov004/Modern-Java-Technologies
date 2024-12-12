package bg.sofia.uni.fmi.mjt.socialnetwork.profile.filter;

import bg.sofia.uni.fmi.mjt.socialnetwork.profile.UserProfile;

import java.util.Set;

public interface Filter {
    Set<UserProfile> getMatches(UserProfile user);
}
