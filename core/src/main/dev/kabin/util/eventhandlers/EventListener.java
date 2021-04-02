package dev.kabin.util.eventhandlers;

@FunctionalInterface
public interface EventListener {

    EventListener EMPTY_EVENT_LISTENER = () -> {
    };

    static EventListener empty() {
        return EMPTY_EVENT_LISTENER;
    }

    void onEvent();

}
