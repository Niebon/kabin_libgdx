package dev.kabin.utilities.eventhandlers;

@FunctionalInterface
public interface EventListener {
    static EventListener doNothing() {
        return () -> {
        };
    }

    void onEvent();
}
