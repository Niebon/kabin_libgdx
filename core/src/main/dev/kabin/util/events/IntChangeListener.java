package dev.kabin.util.events;

/**
 * A change listener for an int; think of this
 * as a modifiable integer that triggers an action each time it is modified.
 */
public interface IntChangeListener {

    int get();

    /**
     * @param value
     * @return true if the value was modified. False otherwise.
     */
    boolean set(int value);

    int last();

    void addListener(int value, Runnable action);

    void clear();

}
