package bg.sofia.uni.fmi.mjt.olympics.comparator;

import bg.sofia.uni.fmi.mjt.olympics.competitor.Competitor;

import java.util.Comparator;

public class CompetitorIdComparator implements Comparator<Competitor> {
    @Override
    public int compare(Competitor o1, Competitor o2) {
        return o1.getIdentifier().compareTo(o2.getIdentifier());
    }
}
