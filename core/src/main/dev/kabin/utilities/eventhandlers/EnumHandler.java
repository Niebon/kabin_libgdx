package dev.kabin.utilities.eventhandlers;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;


/**
 * An interface to provide default implementations for adding listeners {@link #addListener},
 * that react to events being registered {@link #registerEvent}.
 *
 * @param <T> the enum constant that is used.
 */
public interface EnumHandler<T extends Enum<T>> {

    @NotNull Map<T, Collection<EventListener>> getListeners();

    @NotNull Collection<EventListener> getDefaultListeners();

    default void registerEvent(T value) {
        getDefaultListeners().forEach(EventListener::onEvent);
        if (getListeners().containsKey(value)) {
            getListeners().get(value).forEach(EventListener::onEvent);
        }
    }

    /**
     * Adds a listener for a the value T, which gets called
     * every time the given event is registered, i.e., every
     * time a call is made to {@link #registerEvent(Enum)}.
     */
    default void addListener(T value, EventListener eventListener) {
        if (!getListeners().containsKey(value)) {
            getListeners().put(value, new ArrayList<>());
        }
        getListeners().get(value).add(eventListener);
    }

    /**
     * Add event listener for any event of this type.
     */
    default void addListener(EventListener defaultListener) {
        getDefaultListeners().add(defaultListener);
    }

    default void clear() {
        getListeners().clear();
        getDefaultListeners().clear();
    }

}
