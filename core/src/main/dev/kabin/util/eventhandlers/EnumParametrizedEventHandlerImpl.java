package dev.kabin.util.eventhandlers;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

public record EnumParametrizedEventHandlerImpl<T, E extends Enum<E>>(
        ArrayList<Consumer<T>> defaultListeners,
        EnumMap<E, Collection<Consumer<T>>> listeners) implements EnumParameterizedEventHandler<T, E> {

    public static <T, E extends Enum<E>> EnumParametrizedEventHandlerImpl<T, E> of(Class<E> clazz) {
        return new EnumParametrizedEventHandlerImpl<>(
                new ArrayList<>(),
                new EnumMap<>(clazz)
        );
    }

    @Override
    public @NotNull
    Map<E, Collection<Consumer<T>>> getListeners() {
        return listeners;
    }

    @Override
    public @NotNull
    ArrayList<Consumer<T>> getDefaultListeners() {
        return defaultListeners;
    }
}
