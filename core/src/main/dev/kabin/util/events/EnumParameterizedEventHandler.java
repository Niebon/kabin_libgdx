package dev.kabin.util.events;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;


/**
 * An interface to provide default implementations for adding listeners {@link #addListener},
 * that react to events being registered {@link #registerEvent}.
 *
 * @param <E> the enum constant that is used.
 */
public interface EnumParameterizedEventHandler<T, E extends Enum<E>> {

    @NotNull Map<E, Collection<Consumer<T>>> getListeners();

    @NotNull Collection<Consumer<T>> getDefaultListeners();

    default void registerEvent(T parameter, E value) {
        getDefaultListeners().forEach(a -> a.accept(parameter));
        if (getListeners().containsKey(value)) {
            getListeners().get(value).forEach(a -> a.accept(parameter));
        }
    }

    default void addListener(E value, Consumer<T> action) {
        if (!getListeners().containsKey(value)) {
            getListeners().put(value, new ArrayList<>());
        }
        getListeners().get(value).add(action);
    }

    /**
     * Add event listener for any event of this type.
     */
    default void addListener(Consumer<T> action) {
        getDefaultListeners().add(action);
    }

    default void clear() {
        getListeners().clear();
        getDefaultListeners().clear();
    }

}
