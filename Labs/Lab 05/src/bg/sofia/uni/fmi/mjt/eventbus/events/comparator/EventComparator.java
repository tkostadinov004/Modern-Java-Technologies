package bg.sofia.uni.fmi.mjt.eventbus.events.comparator;

import bg.sofia.uni.fmi.mjt.eventbus.events.Event;

import java.util.Comparator;

public class EventComparator<T extends Event<?>> implements Comparator<T> {
    @Override
    public int compare(T o1, T o2) {
        int priorityCompare = Integer.compare(o2.getPriority(), o1.getPriority());
        if (priorityCompare == 0) {
            return o1.getTimestamp().compareTo(o2.getTimestamp());
        }
        return priorityCompare;
    }
}
