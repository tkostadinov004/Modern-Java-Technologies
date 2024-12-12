package bg.sofia.uni.fmi.mjt.socialnetwork.profile.comparator;

import bg.sofia.uni.fmi.mjt.socialnetwork.profile.UserProfile;

import java.util.Comparator;

public class UsersByFriendsCountDescendingComparator implements Comparator<UserProfile> {
    @Override
    public int compare(UserProfile o1, UserProfile o2) {
        return o2.getFriends().size() - o1.getFriends().size();
    }
}
