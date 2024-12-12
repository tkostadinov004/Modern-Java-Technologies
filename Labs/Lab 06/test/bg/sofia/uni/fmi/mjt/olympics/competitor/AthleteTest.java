package bg.sofia.uni.fmi.mjt.olympics.competitor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AthleteTest {
    @Test
    void testAddMedalShouldThrowOnNullMedal() {
        Athlete athlete = new Athlete("ID", "Name", "BUL");
        assertThrows(IllegalArgumentException.class, () -> athlete.addMedal(null));
    }

    @Test
    void testShouldReturnDifferentHashCodeForAthletesWithTheSameName() {
        Athlete athlete1 = new Athlete("ID1", "Name", "BUL");
        Athlete athlete2 = new Athlete("ID2", "Name", "ITA");

        assertNotEquals(athlete1.hashCode(), athlete2.hashCode(),
                "Athletes with the same name but with different ID's should have different hash codes");
    }

    @Test
    void testAthletesWithSameDetailsButDifferentIdShouldNotBeEqual() {
        Athlete athlete1 = new Athlete("ID1", "Name", "BUL");
        Athlete athlete2 = new Athlete("ID2", "Name", "BUL");

        Medal medal = Medal.GOLD;
        athlete1.addMedal(medal);
        athlete2.addMedal(medal);

        assertFalse(athlete1.equals(athlete2),
                "Athletes with the same details but with different ID's should not be equal");
    }
}
