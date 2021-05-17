package dev.kabin.util.events;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

public record EnumEventHandlerImpl<T extends Enum<T>>(
		ArrayList<Runnable> defaultListeners,
		EnumMap<T, Collection<Runnable>> listeners) implements EnumEventHandler<T> {

	public static <T extends Enum<T>> EnumEventHandlerImpl<T> of(Class<T> clazz) {
		return new EnumEventHandlerImpl<>(
				new ArrayList<>(),
				new EnumMap<>(clazz)
		);
	}

	@Override
	public @NotNull
	Map<T, Collection<Runnable>> getListeners() {
		return listeners;
	}

	@Override
	public @NotNull
	ArrayList<Runnable> getDefaultListeners() {
		return defaultListeners;
	}
}
