package bg.sofia.uni.fmi.mjt.eventbus;

import bg.sofia.uni.fmi.mjt.eventbus.events.Event;
import bg.sofia.uni.fmi.mjt.eventbus.events.comparator.TimestampComparator;
import bg.sofia.uni.fmi.mjt.eventbus.exception.MissingSubscriptionException;
import bg.sofia.uni.fmi.mjt.eventbus.subscribers.Subscriber;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class EventBusImpl implements EventBus {
    private final Map<Class<?>, Set<Subscriber<?>>> events;
    private final Map<Class<?>, Set<Event<?>>> eventLogs;
    public EventBusImpl() {
        events = new LinkedHashMap<>();
        eventLogs = new LinkedHashMap<>();
    }

    @Override
    public <T extends Event<?>> void subscribe(Class<T> eventType, Subscriber<? super T> subscriber) {
        if (eventType == null) {
            throw new IllegalArgumentException("Event type is null!");
        }
        if (subscriber == null) {
            throw new IllegalArgumentException("Subscriber is null!");
        }

        if (!events.containsKey(eventType)) {
            events.put(eventType, new LinkedHashSet<>());
        }
        events.get(eventType).add(subscriber);
    }

    @Override
    public <T extends Event<?>> void unsubscribe(Class<T> eventType, Subscriber<? super T> subscriber)
            throws MissingSubscriptionException {
        if (eventType == null) {
            throw new IllegalArgumentException("Event type is null!");
        }
        if (subscriber == null) {
            throw new IllegalArgumentException("Subscriber is null!");
        }
        if (!events.containsKey(eventType) || !events.get(eventType).contains(subscriber)) {
            throw new MissingSubscriptionException("Subscriber is not subscribed to this event!");
        }

        events.get(eventType).remove(subscriber);
    }

    @Override
    public <T extends Event<?>> void publish(T event) {
        if (event == null) {
            throw new IllegalArgumentException("Event is null!");
        }
        eventLogs.putIfAbsent(event.getClass(), new LinkedHashSet<>());
        eventLogs.get(event.getClass()).add(event);

        if (!events.containsKey(event.getClass())) {
            return;
        }
        for (Subscriber<?> subscriber : events.get(event.getClass())) {
            ((Subscriber<? super T>)subscriber).onEvent(event);
        }
    }

    @Override
    public void clear() {
        events.clear();
        eventLogs.clear();
    }

    @Override
    public Collection<? extends Event<?>> getEventLogs(Class<? extends Event<?>> eventType, Instant from, Instant to) {
        if (eventType == null) {
            throw new IllegalArgumentException("Event type is null!");
        }
        if (from == null) {
            throw new IllegalArgumentException("Start timestamp is null!");
        }
        if (to == null) {
            throw new IllegalArgumentException("End timestamp is null!");
        }
        if (from.equals(to)) {
            return Collections.emptySet();
        }
        if (eventLogs.get(eventType) == null) {
            return Collections.emptySet();
        }

        Set<Event<?>> result = new TreeSet<>(new TimestampComparator<>());
        for (var e : eventLogs.get(eventType)) {
            if (!from.isAfter(e.getTimestamp()) && e.getTimestamp().isBefore(to)) {
                result.add(e);
            }
        }
        return Collections.unmodifiableSet(result);
    }

    @Override
    public <T extends Event<?>> Collection<Subscriber<?>> getSubscribersForEvent(Class<T> eventType) {
        if (eventType == null) {
            throw new IllegalArgumentException("Event type is null!");
        }
        if (!events.containsKey(eventType)) {
            return Collections.emptySet();
        }

        return Collections.unmodifiableSet(events.get(eventType));
    }
}
