package bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations.converter;

import bg.sofia.uni.fmi.mjt.splitwise.server.data.CsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.FriendGroup;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.FriendshipRelation;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.dto.FriendGroupDTO;
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

public class FriendGroupConverterTest  {
    private static final CsvProcessor<FriendGroupDTO> CSV_PROCESSOR = mock();
    private static final UserRepository USER_REPOSITORY = mock();
    private static final User USER_1 = new User("USER_1", "asd", "Test", "Test1");
    private static final User USER_2 = new User("USER_2", "asd", "Test", "Test1");
    private static final User USER_3 = new User("USER_3", "asd", "Test", "Test1");
    private static final Collector<FriendGroup, ?, Set<FriendGroup>> COLLECTOR = Collectors.toSet();

    @BeforeAll
    public static void setUp() {
        FriendGroupDTO dto1 = new FriendGroupDTO("group1", Set.of("USER_1", "USER_2"));
        FriendGroupDTO dto2 = new FriendGroupDTO("group2", Set.of());
        FriendGroupDTO dto3 = new FriendGroupDTO("group3", Set.of("user4"));
        FriendGroupDTO dto4 = new FriendGroupDTO("group4", Set.of("USER_3", "user4"));
        FriendGroupDTO dto5 = new FriendGroupDTO("group5", Set.of("USER_3", "USER_2"));
        when(CSV_PROCESSOR.readAll())
                .thenReturn(Set.of(dto1, dto2, dto3, dto4, dto5));
        when(USER_REPOSITORY.getUserByUsername("USER_1")).thenReturn(Optional.of(USER_1));
        when(USER_REPOSITORY.getUserByUsername("USER_2")).thenReturn(Optional.of(USER_2));
        when(USER_REPOSITORY.getUserByUsername("USER_3")).thenReturn(Optional.of(USER_3));
    }

    @Test
    public void testPopulateIgnoresEntriesWithNonExistingUsers() {
        FriendGroupConverter converter = new FriendGroupConverter(CSV_PROCESSOR, USER_REPOSITORY);

        var result = converter.populate(COLLECTOR);

        assertFalse(result.stream().anyMatch(u -> u.participants().stream().anyMatch(user -> user.username().equals("user4"))),
                "Groups with users that are not present in the repository should be ignored");
    }

    @Test
    public void testPopulateIgnoresEmptyGroups() {
        FriendGroupConverter converter = new FriendGroupConverter(CSV_PROCESSOR, USER_REPOSITORY);

        var result = converter.populate(COLLECTOR);

        assertFalse(result.stream().anyMatch(u -> u.participants().isEmpty()),
                "Empty groups should be ignored");
    }

    @Test
    public void testPopulatePopulatesSuccessfully() {
        FriendGroup group1 = new FriendGroup("group1", Set.of(USER_1, USER_2));
        FriendGroup group2 = new FriendGroup("group5", Set.of(USER_3, USER_2));

        FriendGroupConverter converter = new FriendGroupConverter(CSV_PROCESSOR, USER_REPOSITORY);
        var expected = Set.of(group1, group2);
        var actual = converter.populate(COLLECTOR);

        assertEquals(expected, actual,
                "Only entries with users present in the repository should be left and mapped properly");
    }
}

