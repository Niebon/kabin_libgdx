package dev.kabin.util.eventhandlers;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

public record EnumHandlerImpl<T extends Enum<T>>(
		ArrayList<EventListener> defaultListeners,
		EnumMap<T, Collection<EventListener>> listeners) implements EnumHandler<T> {

	public static <T extends Enum<T>> EnumHandlerImpl<T> of(Class<T> clazz) {
		return new EnumHandlerImpl<>(
				new ArrayList<>(),
				new EnumMap<>(clazz)
		);
	}

	@Override
	public @NotNull
	Map<T, Collection<EventListener>> getListeners() {
		return listeners;
	}

	@Override
	public @NotNull
	ArrayList<EventListener> getDefaultListeners() {
		return defaultListeners;
	}
}
