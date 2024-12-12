package bg.sofia.uni.fmi.mjt.eventbus.events.comparator;

import bg.sofia.uni.fmi.mjt.eventbus.events.Event;

import java.util.Comparator;

public class TimestampComparator<T extends Event<?>> implements Comparator<T> {
    @Override
    public int compare(T o1, T o2) {
        return o1.getTimestamp().compareTo(o2.getTimestamp());
    }
}
