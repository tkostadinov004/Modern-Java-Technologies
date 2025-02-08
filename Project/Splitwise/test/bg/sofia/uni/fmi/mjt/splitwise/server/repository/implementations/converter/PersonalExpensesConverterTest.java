package bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations.converter;

import bg.sofia.uni.fmi.mjt.splitwise.server.data.CsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.FriendshipRelation;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.PersonalExpense;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.dto.FriendshipRelationDTO;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.dto.PersonalExpenseDTO;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
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

public class PersonalExpensesConverterTest {
    private static final CsvProcessor<PersonalExpenseDTO> CSV_PROCESSOR = mock();
    private static final UserRepository USER_REPOSITORY = mock();
    private static final User user1 = new User("user1", "asd", "Test", "Test1");
    private static final User user2 = new User("user2", "asd", "Test", "Test1");
    private static final User user3 = new User("user3", "asd", "Test", "Test1");
    private static final Collector<PersonalExpense, ?, Map<User, Set<PersonalExpense>>> COLLECTOR = Collectors.groupingBy(PersonalExpense::payer,
            Collectors.mapping(expense -> expense, Collectors.toSet()));
    private static final PersonalExpenseDTO DTO_1 = new PersonalExpenseDTO("user1", "user2", 100, "test reason1", LocalDateTime.now());
    private static final PersonalExpenseDTO DTO_2 = new PersonalExpenseDTO("user2", "user1", 23, "test reason2", LocalDateTime.now());
    private static final PersonalExpenseDTO DTO_3 = new PersonalExpenseDTO("user1", "user3", 330, "test reason3", LocalDateTime.now());
    private static final PersonalExpenseDTO DTO_4 = new PersonalExpenseDTO("user3", "user1", 440, "test reason4", LocalDateTime.now());
    private static final PersonalExpenseDTO DTO_5 = new PersonalExpenseDTO("user1", "user4", 110, "test reason5", LocalDateTime.now());
    private static final PersonalExpenseDTO DTO_6 = new PersonalExpenseDTO("user4", "user3", 560, "test reason6", LocalDateTime.now());


    @BeforeAll
    public static void setUp() {
        when(CSV_PROCESSOR.readAll())
                .thenReturn(Set.of(DTO_1, DTO_2, DTO_3, DTO_4, DTO_5, DTO_6));
        when(USER_REPOSITORY.getUserByUsername("user1")).thenReturn(Optional.of(user1));
        when(USER_REPOSITORY.getUserByUsername("user2")).thenReturn(Optional.of(user2));
        when(USER_REPOSITORY.getUserByUsername("user3")).thenReturn(Optional.of(user3));
    }

    @Test
    public void testPopulateIgnoresEntriesWithNonExistingUsers() {
        PersonalExpensesConverter converter = new PersonalExpensesConverter(CSV_PROCESSOR, USER_REPOSITORY);

        var result = converter.populate(COLLECTOR);

        assertFalse(result.keySet().stream().anyMatch(u -> u.username().equals("user4")),
                "Expenses with users that are not present in the repository should be ignored");
        assertFalse(result.get(user1).stream().anyMatch(u -> u.debtor().username().equals("user4")),
                "Expenses with users that are not present in the repository should be ignored");
    }

    @Test
    public void testPopulatePopulatesSuccessfully() {
        PersonalExpense expense1 = new PersonalExpense(user1, user2, 100, "test reason1", DTO_1.timestamp());
        PersonalExpense expense2 = new PersonalExpense(user2, user1, 23, "test reason2", DTO_2.timestamp());
        PersonalExpense expense3 = new PersonalExpense(user1, user3, 330, "test reason3", DTO_3.timestamp());
        PersonalExpense expense4 = new PersonalExpense(user3, user1, 440, "test reason4", DTO_4.timestamp());

        PersonalExpensesConverter converter = new PersonalExpensesConverter(CSV_PROCESSOR, USER_REPOSITORY);
        var expected = new HashMap<User, Set<PersonalExpense>>();
        expected.put(user1, Set.of(expense1, expense3));
        expected.put(user2, Set.of(expense2));
        expected.put(user3, Set.of(expense4));

        var actual = converter.populate(COLLECTOR);

        assertEquals(expected, actual,
                "Only entries with users present in the repository should be left and mapped properly");
    }
}
