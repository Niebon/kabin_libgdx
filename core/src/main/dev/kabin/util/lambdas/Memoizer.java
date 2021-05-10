package dev.kabin.util.lambdas;

import java.util.HashMap;

public record Memoizer<T extends Record, R>(HashMap<T, R> memory, Function<T, R> f)
		implements Function<T, R> {

	public R apply(T input) {
		return memory.computeIfAbsent(input, f::apply);
	}
}
