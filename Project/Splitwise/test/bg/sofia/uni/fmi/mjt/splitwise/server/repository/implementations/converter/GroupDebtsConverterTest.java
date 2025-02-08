package bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations.converter;

import bg.sofia.uni.fmi.mjt.splitwise.server.data.CsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.FriendGroup;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.GroupDebt;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.dto.GroupDebtDTO;
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

public class GroupDebtsConverterTest {
    private static final CsvProcessor<GroupDebtDTO> CSV_PROCESSOR = mock();
    private static final UserRepository USER_REPOSITORY = mock();
    private static final FriendGroupRepository GROUP_REPOSITORY = mock();

    private static final User USER_1 = new User("USER_1", "asd", "Test", "Test1");
    private static final User USER_2 = new User("USER_2", "asd", "Test", "Test1");
    private static final User USER_3 = new User("USER_3", "asd", "Test", "Test1");
    private static final FriendGroup GROUP = new FriendGroup("testGroup", Set.of(USER_1, USER_2));
    private static final Collector<GroupDebt, ?, Map<FriendGroup, Set<GroupDebt>>> COLLECTOR = Collectors.groupingBy(GroupDebt::group,
            Collectors.mapping(debt -> debt, Collectors.toSet()));
   
    @BeforeAll
    public static void setUp() { 
        GroupDebtDTO dto1 = new GroupDebtDTO("USER_1", "USER_2", "testGroup", 100, "test reason1");
        GroupDebtDTO dto2 = new GroupDebtDTO("USER_1","USER_3", "testGroup", 200, "test reason2");
        GroupDebtDTO dto3 = new GroupDebtDTO("USER_2", "USER_1","testGroup",1500, "test reason3");
        GroupDebtDTO dto4 = new GroupDebtDTO("user4", "USER_1","testGroup",1500, "test reason4");
        GroupDebtDTO dto5 = new GroupDebtDTO("USER_3", "USER_1","testGroup",1500, "test reason5");
        GroupDebtDTO dto6 = new GroupDebtDTO("USER_1", "USER_2","testGroup123", 1500, "test reason6");
        GroupDebtDTO dto7 = new GroupDebtDTO("USER_2", "USER_2","testGroup", 1500, "test reason7");
        GroupDebtDTO dto8 = new GroupDebtDTO("USER_1", "user4","testGroup", 1500, "test reason8");

        when(CSV_PROCESSOR.readAll())
                .thenReturn(Set.of(dto1, dto2, dto3, dto4, dto5, dto6, dto7, dto8));
        when(USER_REPOSITORY.getUserByUsername("USER_1")).thenReturn(Optional.of(USER_1));
        when(USER_REPOSITORY.getUserByUsername("USER_2")).thenReturn(Optional.of(USER_2));
        when(USER_REPOSITORY.getUserByUsername("USER_3")).thenReturn(Optional.of(USER_3));
        when(GROUP_REPOSITORY.getGroup("testGroup")).thenReturn(Optional.of(GROUP));
    }

    @Test
    public void testPopulateIgnoresEntriesWithNonExistingUsers() {
        GroupDebtsConverter converter = new GroupDebtsConverter(CSV_PROCESSOR, USER_REPOSITORY, GROUP_REPOSITORY);

        var result = converter.populate(COLLECTOR);

        assertFalse(result.values().stream().flatMap(Collection::stream).anyMatch(u -> u.debtor().username().equals("user4")),
                "Group debts with debtors that are not present in the repository should be ignored");
        assertFalse(result.values().stream().flatMap(Collection::stream).anyMatch(u -> u.recipient().username().equals("user4")),
                "Group recipient with debtors that are not present in the repository should be ignored");
    }

    @Test
    public void testPopulateIgnoresEntriesWithSameUsers() {
        GroupDebtsConverter converter = new GroupDebtsConverter(CSV_PROCESSOR, USER_REPOSITORY, GROUP_REPOSITORY);

        var result = converter.populate(COLLECTOR);

        assertFalse(result.values().stream().flatMap(Collection::stream).anyMatch(u -> u.debtor().username().equals("user4") &&
                u.recipient().username().equals("user4")),
                "Group debts with debtors and recipients that are the same should be ignored");
    }

    @Test
    public void testPopulateIgnoresEntriesWithNonExistingGroups() {
        GroupDebtsConverter converter = new GroupDebtsConverter(CSV_PROCESSOR, USER_REPOSITORY, GROUP_REPOSITORY);

        var result = converter.populate(COLLECTOR);

        assertFalse(result.values().stream().flatMap(Collection::stream).anyMatch(u -> u.group().name().equals("testGroup123")),
                "Group debts with groups that are not present in the repository should be ignored");
    }

    @Test
    public void testPopulateIgnoresEntriesWithUsersNotBelongingToGivenGroup() {
        GroupDebtsConverter converter = new GroupDebtsConverter(CSV_PROCESSOR, USER_REPOSITORY, GROUP_REPOSITORY);

        var result = converter.populate(COLLECTOR);

        assertFalse(result.values().stream().flatMap(Collection::stream).anyMatch(u -> u.debtor().username().equals("USER_3") && u.group().name().equals("testGroup")),
                "Group debts with users that are not members of their given group in the repository should be ignored");
    }

    @Test
    public void testPopulatePopulatesSuccessfully() {
        GroupDebt debt1 = new GroupDebt(USER_1, USER_2, GROUP, 100, "test reason1");
        GroupDebt debt2 = new GroupDebt(USER_2, USER_1, GROUP,1500, "test reason3");
        GroupDebtsConverter converter = new GroupDebtsConverter(CSV_PROCESSOR, USER_REPOSITORY, GROUP_REPOSITORY);

        var expected = new HashMap<FriendGroup, Set<GroupDebt>>();
        expected.put(GROUP, Set.of(debt1, debt2));

        var actual = converter.populate(COLLECTOR);

        assertEquals(expected, actual,
                "Only entries with users and groups present in the repository should be left and mapped properly");
    }
}