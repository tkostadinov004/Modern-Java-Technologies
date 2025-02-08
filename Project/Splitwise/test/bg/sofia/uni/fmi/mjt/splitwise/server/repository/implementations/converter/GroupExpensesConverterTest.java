package bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations.converter;

import bg.sofia.uni.fmi.mjt.splitwise.server.data.CsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.FriendGroup;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.GroupExpense;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.dto.GroupExpenseDTO;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.FriendGroupRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collection;
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

public class GroupExpensesConverterTest {
    private static final CsvProcessor<GroupExpenseDTO> CSV_PROCESSOR = mock();
    private static final UserRepository USER_REPOSITORY = mock();
    private static final FriendGroupRepository GROUP_REPOSITORY = mock();

    private static final User USER_1 = new User("USER_1", "asd", "Test", "Test1");
    private static final User USER_2 = new User("USER_2", "asd", "Test", "Test1");
    private static final User USER_3 = new User("USER_3", "asd", "Test", "Test1");
    private static final FriendGroup GROUP = new FriendGroup("testGroup", Set.of(USER_1, USER_2));
    private static final Collector<GroupExpense, ?, Map<User, Set<GroupExpense>>> COLLECTOR = Collectors.groupingBy(GroupExpense::payer,
            Collectors.mapping(expense -> expense, Collectors.toSet()));
    private static final GroupExpenseDTO DTO_1 = new GroupExpenseDTO("USER_1", 100, "test reason1", "testGroup", LocalDateTime.now());
    private static final GroupExpenseDTO DTO_2 = new GroupExpenseDTO("USER_1", 200, "test reason2", "testGroup", LocalDateTime.now());
    private static final GroupExpenseDTO DTO_3 = new GroupExpenseDTO("USER_2", 1500, "test reason3", "testGroup", LocalDateTime.now());
    private static final GroupExpenseDTO DTO_4 = new GroupExpenseDTO("user4", 1500, "test reason4", "testGroup", LocalDateTime.now());
    private static final GroupExpenseDTO DTO_5 = new GroupExpenseDTO("USER_3", 1500, "test reason5", "testGroup", LocalDateTime.now());
    private static final GroupExpenseDTO DTO_6 = new GroupExpenseDTO("USER_1", 1500, "test reason6", "testGroup123", LocalDateTime.now());

    @BeforeAll
    public static void setUp() {
        when(CSV_PROCESSOR.readAll())
                .thenReturn(Set.of(DTO_1, DTO_2, DTO_3, DTO_4, DTO_5, DTO_6));
        when(USER_REPOSITORY.getUserByUsername("USER_1")).thenReturn(Optional.of(USER_1));
        when(USER_REPOSITORY.getUserByUsername("USER_2")).thenReturn(Optional.of(USER_2));
        when(USER_REPOSITORY.getUserByUsername("USER_3")).thenReturn(Optional.of(USER_3));
        when(GROUP_REPOSITORY.getGroup("testGroup")).thenReturn(Optional.of(GROUP));
    }

    @Test
    public void testPopulateIgnoresEntriesWithNonExistingUsers() {
        GroupExpensesConverter converter = new GroupExpensesConverter(CSV_PROCESSOR, USER_REPOSITORY, GROUP_REPOSITORY);

        var result = converter.populate(COLLECTOR);

        assertFalse(result.keySet().stream().anyMatch(u -> u.username().equals("user4")),
                "Expenses with users that are not present in the repository should be ignored");
    }

    @Test
    public void testPopulateIgnoresEntriesWithNonExistingGroups() {
        GroupExpensesConverter converter = new GroupExpensesConverter(CSV_PROCESSOR, USER_REPOSITORY, GROUP_REPOSITORY);

        var result = converter.populate(COLLECTOR);

        assertFalse(result.values().stream().flatMap(Collection::stream).anyMatch(u -> u.group().name().equals("testGroup123")),
                "Expenses with groups that are not present in the repository should be ignored");
    }

    @Test
    public void testPopulateIgnoresEntriesWithUsersNotBelongingToGivenGroup() {
        GroupExpensesConverter converter = new GroupExpensesConverter(CSV_PROCESSOR, USER_REPOSITORY, GROUP_REPOSITORY);

        var result = converter.populate(COLLECTOR);

        assertFalse(result.values().stream().flatMap(Collection::stream).anyMatch(u -> u.payer().username().equals("USER_3") && u.group().name().equals("testGroup")),
                "Expenses with users that are not members of their given group in the repository should be ignored");
    }

    @Test
    public void testPopulatePopulatesSuccessfully() {
        GroupExpense expense1 = new GroupExpense(USER_1, 100, "test reason1", GROUP, DTO_1.timestamp());
        GroupExpense expense2 = new GroupExpense(USER_1, 200, "test reason2", GROUP, DTO_2.timestamp());
        GroupExpense expense3 = new GroupExpense(USER_2, 1500, "test reason3", GROUP, DTO_3.timestamp());

        GroupExpensesConverter converter = new GroupExpensesConverter(CSV_PROCESSOR, USER_REPOSITORY, GROUP_REPOSITORY);
        var expected = new HashMap<User, Set<GroupExpense>>();
        expected.put(USER_1, Set.of(expense1, expense2));
        expected.put(USER_2, Set.of(expense3));

        var actual = converter.populate(COLLECTOR);

        assertEquals(expected, actual,
                "Only entries with users and groups present in the repository should be left and mapped properly");
    }
}

