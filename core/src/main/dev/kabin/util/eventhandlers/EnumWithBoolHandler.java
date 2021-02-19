package dev.kabin.util.eventhandlers;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Represents an event handler for events defined by an enum value together with a boolean state (value, state).
 * For example (key-board-button, pressed) or (mouse-button, pressed).
 * <p>
 * The listeners added to each event will fire each time the method {@link #registerEvent(Enum, boolean)} is called.
 * To add listeners, use {@link #addListener(Enum, boolean, EventListener)} and {@link #addListener(EventListener)}.
 *
 * @param <T> the enum constant that is used.
 */
public interface EnumWithBoolHandler<T extends Enum<T>> {

    @NotNull Map<T, Boolean> getCurrentStates();

    @NotNull Map<T, List<EventListener>> getListenersPressed();

    @NotNull Map<T, List<EventListener>> getListenersReleased();

    @NotNull List<EventListener> getChangeListeners();

    @NotNull List<EventListener> getDefaultListeners();

    default void registerEvent(@NotNull T value, boolean pressed) {
        final Boolean valueCurrentState;
        if ((valueCurrentState = getCurrentStates().get(value)) != null && valueCurrentState == pressed) {
            return;
        }
        if (valueCurrentState == null) {
            getCurrentStates().put(value, pressed);
            makeCallToListeners(value, pressed, !pressed);
        } else {
            final boolean valueOldState = valueCurrentState;
            getCurrentStates().put(value, pressed);
            makeCallToListeners(value, pressed, valueOldState);
        }
    }

    /**
     * Makes the recursive calls to the listeners.
     *
     * @param value the value on which the listeners listen.
     */
    private void makeCallToListeners(@NotNull T value, boolean pressed, boolean oldValue) {
        makeCallToListeners(getDefaultListeners());

        if (pressed != oldValue) {
            makeCallToListeners(getChangeListeners());
        }

        if (pressed && getListenersPressed().containsKey(value)) {
            makeCallToListeners(getListenersPressed().get(value));
        } else if (getListenersReleased().containsKey(value)) {
            makeCallToListeners(getListenersReleased().get(value));
        }
    }

    private void makeCallToListeners(@NotNull List<EventListener> listeners) {
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0, n = listeners.size(); i < n; i++) {
            listeners.get(i).onEvent();
        }
    }

    default boolean isPressed(@NotNull T t) {
        return getCurrentStates().containsKey(t) && getCurrentStates().get(t);
    }

    /**
     * Adds a listener for a key event (keycode, pressed), which gets called
     * every time the given event occurs.
     */
    default void addListener(@NotNull T value, boolean pressed, @NotNull EventListener eventListener) {
        if (pressed) {
            if (!getListenersPressed().containsKey(value)) {
                getListenersPressed().put(value, new ArrayList<>());
            }
            getListenersPressed().get(value).add(eventListener);
        } else {
            if (!getListenersReleased().containsKey(value)) {
                getListenersReleased().put(value, new ArrayList<>());
            }
            getListenersReleased().get(value).add(eventListener);
        }
    }

    /**
     * Add event listener for any event of this type.
     */
    default void addListener(@NotNull EventListener defaultListener) {
        getDefaultListeners().add(defaultListener);
    }

    default void addChangeListener(@NotNull EventListener changeListener) {
        getChangeListeners().add(changeListener);
    }

    /**
     * Clears all listeners.
     */
    default void clear() {
        getChangeListeners().clear();
        getCurrentStates().clear();
        getListenersPressed().clear();
        getListenersReleased().clear();
        getDefaultListeners().clear();
    }
}
