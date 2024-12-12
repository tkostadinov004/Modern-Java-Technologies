package bg.sofia.uni.fmi.mjt.olympics.competition;

import bg.sofia.uni.fmi.mjt.olympics.competitor.Athlete;
import bg.sofia.uni.fmi.mjt.olympics.competitor.Competitor;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

public class CompetitionTest {
    @Test
    void testCompetitorThrowsOnInvalidName() {
        Athlete athlete = new Athlete("ID", "Name", "BUL");
        assertThrows(IllegalArgumentException.class, () -> new Competition("", "Swimming", Set.of(athlete)));
        assertThrows(IllegalArgumentException.class, () -> new Competition(null, "Swimming", Set.of(athlete)));
    }
    @Test
    void testCompetitorThrowsOnInvalidNationality() {
        Athlete athlete = new Athlete("ID", "Name", "BUL");
        assertThrows(IllegalArgumentException.class, () -> new Competition("Name", "", Set.of(athlete)));
        assertThrows(IllegalArgumentException.class, () -> new Competition("Name", null, Set.of(athlete)));
    }
    @Test
    void testCompetitorThrowsOnInvalidSetOfCompetitors() {
        assertThrows(IllegalArgumentException.class, () -> new Competition("Name", "Swimming", Set.of()));
        assertThrows(IllegalArgumentException.class, () -> new Competition("Name", "Swimming", null));
    }
    @Test
    void testSameCompetitionsHaveSameHashCode() {
        Competition c1 = new Competition("A", "B", Set.of(new Athlete("A", "B", "C")));
        Competition c2 = new Competition("A", "B", Set.of(new Athlete("A", "B", "C")));

        assertEquals(c1.hashCode(), c2.hashCode());
        Competition c3 = new Competition("A", "B", Set.of(new Athlete("A", "B", "C"), new Athlete("D", "E", "F")));
        assertNotEquals(c1.hashCode(), c3.hashCode());
        assertNotEquals(c2.hashCode(), c3.hashCode());
        Competition c4 = new Competition("A", "S", Set.of(new Athlete("A", "B", "C")));
        assertNotEquals(c1.hashCode(), c4.hashCode());
        assertNotEquals(c2.hashCode(), c4.hashCode());
        assertNotEquals(c3.hashCode(), c4.hashCode());
    }
    @Test
    void testSameCompetitionsAreEqual() {
        Competition c1 = new Competition("A", "B", Set.of(new Athlete("A", "B", "C")));
        Competition c2 = new Competition("A", "B", Set.of(new Athlete("A", "B", "C")));

        assertEquals(c1, c2);
        Competition c3 = new Competition("A", "B", Set.of(new Athlete("A", "B", "C"), new Athlete("D", "E", "F")));
        assertNotEquals(c1, c3);
        assertNotEquals(c2, c3);
        Competition c4 = new Competition("A", "S", Set.of(new Athlete("A", "B", "C")));
        assertNotEquals(c1, c4);
        assertNotEquals(c2, c4);
        assertNotEquals(c3, c4);
    }
}
