package dev.kabin.util.eventhandlers;

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
public interface EnumEventHandler<T extends Enum<T>> {

    @NotNull Map<T, Collection<Runnable>> getListeners();

    @NotNull Collection<Runnable> getDefaultListeners();

    default void registerEvent(T value) {
        getDefaultListeners().forEach(Runnable::run);
        if (getListeners().containsKey(value)) {
            getListeners().get(value).forEach(Runnable::run);
        }
    }

    /**
     * Adds a listener for a the value T, which gets called
     * every time the given event is registered, i.e., every
     * time a call is made to {@link #registerEvent(Enum)}.
     */
    default void addListener(T value, Runnable action) {
        if (!getListeners().containsKey(value)) {
            getListeners().put(value, new ArrayList<>());
        }
        getListeners().get(value).add(action);
    }

    /**
     * Add event listener for any event of this type.
     */
    default void addListener(Runnable action) {
        getDefaultListeners().add(action);
    }

    default void clear() {
        getListeners().clear();
        getDefaultListeners().clear();
    }

}
