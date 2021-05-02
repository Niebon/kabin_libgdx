package dev.kabin.util;


import dev.kabin.util.fp.Function;

/**
 * A named wrapper for an object.
 *
 * @param <T> the type of the object.
 */
public record NamedObj<T>(T obj, String name) {

    public <R> NamedObj<R> map(Function<T, R> f) {
        return new NamedObj<>(f.apply(obj), name);
    }

}
