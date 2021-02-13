package dev.kabin.utilities.eventhandlers;

@FunctionalInterface
public interface EventListener {

    static EventListener empty() {
        return () -> {
        };
    }

    void onEvent();

}
