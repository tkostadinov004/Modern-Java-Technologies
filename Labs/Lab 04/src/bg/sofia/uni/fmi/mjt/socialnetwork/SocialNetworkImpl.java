package bg.sofia.uni.fmi.mjt.socialnetwork;

import bg.sofia.uni.fmi.mjt.socialnetwork.exception.UserRegistrationException;
import bg.sofia.uni.fmi.mjt.socialnetwork.post.Post;
import bg.sofia.uni.fmi.mjt.socialnetwork.post.SocialFeedPost;
import bg.sofia.uni.fmi.mjt.socialnetwork.profile.DefaultUserProfile;
import bg.sofia.uni.fmi.mjt.socialnetwork.profile.Interest;
import bg.sofia.uni.fmi.mjt.socialnetwork.profile.UserProfile;
import bg.sofia.uni.fmi.mjt.socialnetwork.profile.comparator.UsersByFriendsCountDescendingComparator;
import bg.sofia.uni.fmi.mjt.socialnetwork.profile.filter.Filter;
import bg.sofia.uni.fmi.mjt.socialnetwork.profile.filter.UserByFriendNetworkFilter;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class SocialNetworkImpl implements SocialNetwork {
    private final Set<UserProfile> users = new LinkedHashSet<>();
    private final Map<UserProfile, Set<Post>> postsByUser = new LinkedHashMap<>();
    private final Set<Post> posts = new LinkedHashSet<>();

    @Override
    public void registerUser(UserProfile userProfile) throws UserRegistrationException {
        if (userProfile == null) {
            throw new IllegalArgumentException("User profile cannot be null!");
        }
        if (users.contains(userProfile)) {
            throw new UserRegistrationException("User is already registered!");
        }
        users.add(userProfile);
    }

    @Override
    public Set<UserProfile> getAllUsers() {
        return Collections.unmodifiableSet(users);
    }

    @Override
    public Post post(UserProfile userProfile, String content) throws UserRegistrationException {
        if (userProfile == null) {
            throw new IllegalArgumentException("User profile cannot be null!");
        }
        if (content == null || content.isEmpty()) {
            throw new IllegalArgumentException("Content cannot be null or empty!");
        }
        if (!users.contains(userProfile)) {
            throw new UserRegistrationException("User is not registered!");
        }

        postsByUser.putIfAbsent(userProfile, new LinkedHashSet<>());

        Post post = new SocialFeedPost(userProfile, content);
        postsByUser.get(userProfile).add(post);
        posts.add(post);

        return post;
    }

    @Override
    public Collection<Post> getPosts() {
        return Collections.unmodifiableSet(posts);
    }

    @Override
    public Set<UserProfile> getReachedUsers(Post post) {
        if (post == null) {
            throw new IllegalArgumentException("Post cannot be null!");
        }
        UserProfile author = post.getAuthor();

        Filter filter = new UserByFriendNetworkFilter();
        Set<UserProfile> reachedUsers = filter.getMatches(author);

        return Collections.unmodifiableSet(reachedUsers);
    }

    @Override
    public Set<UserProfile> getMutualFriends(UserProfile userProfile1, UserProfile userProfile2)
            throws UserRegistrationException {
        if (userProfile1 == null || userProfile2 == null) {
            throw new IllegalArgumentException("User profile cannot be null!");
        }
        if (!users.contains(userProfile1) || !users.contains(userProfile2)) {
            throw new UserRegistrationException("User is not registered!");
        }
        Set<UserProfile> mutualFriends = new LinkedHashSet<>(userProfile1.getFriends());
        mutualFriends.retainAll(userProfile2.getFriends());
        return mutualFriends;
    }

    @Override
    public SortedSet<UserProfile> getAllProfilesSortedByFriendsCount() {
        SortedSet<UserProfile> result = new TreeSet<>(new UsersByFriendsCountDescendingComparator());
        result.addAll(users);
        return result;
    }
}

class Main {
    public static void main(String[] args) throws UserRegistrationException {
        
        UserProfile u1 = new DefaultUserProfile("u1");
        UserProfile u2 = new DefaultUserProfile("u2");
        UserProfile u3 = new DefaultUserProfile("u3");
        UserProfile u4 = new DefaultUserProfile("u4");
        UserProfile u5 = new DefaultUserProfile("u5");
        UserProfile u6 = new DefaultUserProfile("u6");
        u1.addInterest(Interest.BOOKS);
        u1.addInterest(Interest.FOOD);
        u1.addInterest(Interest.MOVIES);
        u3.addInterest(Interest.GAMES);
        u3.addInterest(Interest.MUSIC);
        u3.addInterest(Interest.BOOKS);
        u1.addFriend(u2);
        u2.addFriend(u3);
        SocialNetwork sn = new SocialNetworkImpl();
        sn.registerUser(u1);
        sn.registerUser(u2);
        sn.registerUser(u3);
        sn.registerUser(u4);
        sn.registerUser(u5);
        sn.registerUser(u6);
        sn.post(u1, "asdasd");
        for (var user : sn.getReachedUsers((Post)sn.getPosts().toArray()[0])) {
            System.out.println(user);
        }
    }
}