package bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations.converter;

import bg.sofia.uni.fmi.mjt.splitwise.server.data.CsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.PersonalDebt;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.dto.PersonalDebtDTO;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PersonalDebtsConverterTest {
    private static final CsvProcessor<PersonalDebtDTO> CSV_PROCESSOR = mock();
    private static final UserRepository USER_REPOSITORY = mock();
    private static final User USER_1 = new User("USER_1", "asd", "Test", "Test1");
    private static final User USER_2 = new User("USER_2", "asd", "Test", "Test1");
    private static final User USER_3 = new User("USER_3", "asd", "Test", "Test1");
    private static final Collector<PersonalDebt, ?, Set<PersonalDebt>> COLLECTOR =
            Collectors.toSet();

    @BeforeAll
    public static void setUp() {
        PersonalDebtDTO dto1 = new PersonalDebtDTO("USER_1", "USER_2", 100, "test reason1");
        PersonalDebtDTO dto2 = new PersonalDebtDTO("USER_2", "USER_1", 23, "test reason2");
        PersonalDebtDTO dto3 = new PersonalDebtDTO("USER_1", "USER_3", 330, "test reason3");
        PersonalDebtDTO dto4 = new PersonalDebtDTO("USER_3", "USER_1", 440, "test reason4");
        PersonalDebtDTO dto5 = new PersonalDebtDTO("USER_1", "user4", 110, "test reason5");
        PersonalDebtDTO dto6 = new PersonalDebtDTO("user4", "USER_3", 560, "test reason6");
        PersonalDebtDTO dto7 = new PersonalDebtDTO("USER_2", "USER_2", 560, "test reason6");

        when(CSV_PROCESSOR.readAll())
                .thenReturn(Set.of(dto1, dto2, dto3, dto4, dto5, dto6, dto7));
        when(USER_REPOSITORY.getUserByUsername("USER_1")).thenReturn(Optional.of(USER_1));
        when(USER_REPOSITORY.getUserByUsername("USER_2")).thenReturn(Optional.of(USER_2));
        when(USER_REPOSITORY.getUserByUsername("USER_3")).thenReturn(Optional.of(USER_3));
    }

    @Test
    public void testPopulateIgnoresEntriesWithNonExistingUsers() {
        PersonalDebtsConverter converter = new PersonalDebtsConverter(CSV_PROCESSOR, USER_REPOSITORY);

        var result = converter.populate(COLLECTOR);

        assertFalse(result.stream().anyMatch(u -> u.debtor().username().equals("user4")),
                "Personal debts with debtors that are not present in the repository should be ignored");
        assertFalse(result.stream().anyMatch(u -> u.recipient().username().equals("user4")),
                "Personal debts with recipients that are not present in the repository should be ignored");
    }

    @Test
    public void testPopulateIgnoresEntriesWithSameUsers() {
        PersonalDebtsConverter converter = new PersonalDebtsConverter(CSV_PROCESSOR, USER_REPOSITORY);

        var result = converter.populate(COLLECTOR);

        assertFalse(result.stream().anyMatch(u -> u.debtor().username().equals("USER_2")
                && u.recipient().username().equals("USER_2")),
                "Personal debts with debtors and recipients that are the same should be ignored");
    }

    @Test
    public void testPopulatePopulatesSuccessfully() {
        PersonalDebt debt1 = new PersonalDebt(USER_1, USER_2, 100, "test reason1");
        PersonalDebt debt2 = new PersonalDebt(USER_2, USER_1, 23, "test reason2");
        PersonalDebt debt3 = new PersonalDebt(USER_1, USER_3, 330, "test reason3");
        PersonalDebt debt4 = new PersonalDebt(USER_3, USER_1, 440, "test reason4");

        PersonalDebtsConverter converter = new PersonalDebtsConverter(CSV_PROCESSOR, USER_REPOSITORY);
        var expected = Set.of(debt1, debt2, debt3, debt4);

        var actual = converter.populate(COLLECTOR);

        assertEquals(expected, actual,
                "Only entries with users present in the repository should be left and mapped properly");
    }
}
