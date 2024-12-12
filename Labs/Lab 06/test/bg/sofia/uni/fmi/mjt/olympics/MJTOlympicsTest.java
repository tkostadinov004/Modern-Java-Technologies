package bg.sofia.uni.fmi.mjt.olympics;

import bg.sofia.uni.fmi.mjt.olympics.comparator.CompetitorIdComparator;
import bg.sofia.uni.fmi.mjt.olympics.competition.Competition;
import bg.sofia.uni.fmi.mjt.olympics.competition.CompetitionResultFetcher;
import bg.sofia.uni.fmi.mjt.olympics.competitor.Athlete;
import bg.sofia.uni.fmi.mjt.olympics.competitor.Competitor;
import bg.sofia.uni.fmi.mjt.olympics.competitor.Medal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MJTOlympicsTest {
    private CompetitionResultFetcher fetcher = mock();
    @BeforeEach
    void setUp() {

    }

    @Test
    void testUpdateStatisticsThrowsOnUnregisteredCompetitor() {
        MJTOlympics olympics = new MJTOlympics(Set.of(), fetcher);

        Athlete athlete = new Athlete("ID", "Name", "BUL");
        Competition competition = new Competition("SampleName", "SampleDiscipline", Set.of(athlete));
        assertThrows(IllegalArgumentException.class, () -> olympics.updateMedalStatistics(competition));
    }

    @Test
    void testUpdateStatisticsForAllCompetitors() {
        Athlete athlete = new Athlete("ID", "Name", "BUL");
        athlete.addMedal(Medal.GOLD);
        Athlete athlete1 = new Athlete("ID1", "Name1", "BUL");
        athlete1.addMedal(Medal.SILVER);
        Athlete athlete2 = new Athlete("ID2", "Name2", "BUL");
        athlete2.addMedal(Medal.BRONZE);
        Athlete athlete3 = new Athlete("ID3", "Name3", "BUL");
        athlete3.addMedal(Medal.BRONZE);
        TreeSet<Competitor> competitors = new TreeSet<>(new CompetitorIdComparator());
        competitors.addAll(Set.of(athlete, athlete1, athlete2, athlete3));
        Competition competition = new Competition("SampleName", "SampleDiscipline", competitors);
        MJTOlympics olympics = new MJTOlympics(competitors, fetcher);

        when(fetcher.getResult(competition)).thenReturn(competitors);
        olympics.updateMedalStatistics(competition);

        assertEquals(4, olympics.getTotalMedals("BUL"));
    }

    @Test
    void testUpdateStatisticsThrowsIfNull() {
        MJTOlympics olympics = new MJTOlympics(Set.of(), fetcher);

        assertThrows(IllegalArgumentException.class, () -> olympics.updateMedalStatistics(null));
    }

    @Test
    void testThrowsOnInvalidCountry() {
        MJTOlympics olympics = new MJTOlympics(Set.of(), fetcher);

        assertThrows(IllegalArgumentException.class, () -> olympics.getTotalMedals(null));
        assertThrows(IllegalArgumentException.class, () -> olympics.getTotalMedals("North Korea"));
    }
}
