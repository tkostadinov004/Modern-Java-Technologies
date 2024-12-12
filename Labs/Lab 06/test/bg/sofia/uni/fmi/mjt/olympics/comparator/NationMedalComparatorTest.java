package bg.sofia.uni.fmi.mjt.olympics.comparator;

import bg.sofia.uni.fmi.mjt.olympics.MJTOlympics;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NationMedalComparatorTest {
    private MJTOlympics olympicsMock = mock();
    @BeforeEach
    void setUp() {
        when(olympicsMock.getTotalMedals("USA")).thenReturn(5);
        when(olympicsMock.getTotalMedals("BUL")).thenReturn(5);
    }
    @Test
    void testCompareWhenTwoNationsHaveTheSameMedalsDescendingAlpha() {
        NationMedalComparator comparator = new NationMedalComparator(olympicsMock);
        assertTrue(comparator.compare("USA", "BUL") > 0,
                "Two countries with the same amount of medals should be compared by their names");
    }
    @Test
    void testCompareWhenTwoNationsHaveTheSameMedalsAscendingAlpha() {
        NationMedalComparator comparator = new NationMedalComparator(olympicsMock);
        assertTrue(comparator.compare("BUL", "USA") < 0,
                "Two countries with the same amount of medals should be compared by their names");
    }
    @Test
    void testCompareWhenTwoNationsHaveDifferentAmountOfMedals() {
        when(olympicsMock.getTotalMedals("ITA")).thenReturn(6);

        NationMedalComparator comparator = new NationMedalComparator(olympicsMock);
        assertTrue(comparator.compare("BUL", "ITA") < 0,
                "Two countries with different amounts of medals should be compared by the count of their medals");
        assertTrue(comparator.compare("ITA", "USA") > 0,
                "Two countries with different amounts of medals should be compared by the count of their medals");

    }
}
