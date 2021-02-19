package dev.kabin.util.eventhandlers;

@FunctionalInterface
public interface EventListener {

    static EventListener empty() {
        return () -> {
        };
    }

    void onEvent();

}
