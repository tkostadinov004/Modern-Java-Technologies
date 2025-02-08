package bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations.converter;

import bg.sofia.uni.fmi.mjt.splitwise.server.data.CsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.FriendshipRelation;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.dto.FriendshipRelationDTO;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserFriendsConverterTest {
    private static final CsvProcessor<FriendshipRelationDTO> CSV_PROCESSOR = mock();
    private static final UserRepository USER_REPOSITORY = mock();
    private static final User USER_1 = new User(" USER_1", "asd", "Test", "Test1");
    private static final User USER_2 = new User(" USER_2", "asd", "Test", "Test1");
    private static final User USER_3 = new User(" USER_3", "asd", "Test", "Test1");
    private static final Collector<FriendshipRelation, ?, Map<User, Set<User>>> COLLECTOR = Collectors.groupingBy(FriendshipRelation::first,
            Collectors.mapping(FriendshipRelation::second, Collectors.toSet()));

    @BeforeAll
    public static void setUp() {
        FriendshipRelationDTO dto1 = new FriendshipRelationDTO(" USER_1", " USER_2");
        FriendshipRelationDTO dto2 = new FriendshipRelationDTO(" USER_2", " USER_1");
        FriendshipRelationDTO dto3 = new FriendshipRelationDTO(" USER_1", " USER_3");
        FriendshipRelationDTO dto4 = new FriendshipRelationDTO(" USER_3", " USER_1");
        FriendshipRelationDTO dto5 = new FriendshipRelationDTO(" USER_1", "user4");
        FriendshipRelationDTO dto6 = new FriendshipRelationDTO("user4", " USER_3");

        when(CSV_PROCESSOR.readAll())
                .thenReturn(Set.of(dto1, dto2, dto3, dto4, dto5, dto6));
        when(USER_REPOSITORY.getUserByUsername(" USER_1")).thenReturn(Optional.of( USER_1));
        when(USER_REPOSITORY.getUserByUsername(" USER_2")).thenReturn(Optional.of( USER_2));
        when(USER_REPOSITORY.getUserByUsername(" USER_3")).thenReturn(Optional.of( USER_3));
    }

    @Test
    public void testPopulateIgnoresEntriesWithNonExistingUsers() {
        UserFriendsConverter converter = new UserFriendsConverter(CSV_PROCESSOR, USER_REPOSITORY);

        var result = converter.populate(COLLECTOR);

        assertFalse(result.keySet().stream().anyMatch(u -> u.username().equals("user4")),
                "Relations with users that are not present in the repository should be ignored");
        assertFalse(result.get(USER_1).stream().anyMatch(u -> u.username().equals("user4")),
                "Relations with users that are not present in the repository should be ignored");
    }

    @Test
    public void testPopulatePopulatesSuccessfully() {
        UserFriendsConverter converter = new UserFriendsConverter(CSV_PROCESSOR, USER_REPOSITORY);
        var expected = new HashMap<User, Set<User>>();
        expected.put(USER_1, Set.of(USER_2, USER_3));
        expected.put(USER_2, Set.of(USER_1));
        expected.put(USER_3, Set.of(USER_1));

        var actual = converter.populate(COLLECTOR);

        assertEquals(expected, actual,
                "Only entries with users present in the repository should be left and mapped properly");
    }
}
