package dev.kabin.util;


import dev.kabin.util.lambdas.Function;

/**
 * A named wrapper for an object.
 *
 * @param <T> the type of the object.
 */
public record NamedObj<T>(String name, T obj) {

    public <R> NamedObj<R> map(Function<T, R> f) {
        return new NamedObj<>(name, f.apply(obj));
    }

}
