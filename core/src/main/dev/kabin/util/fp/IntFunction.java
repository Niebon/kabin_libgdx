package dev.kabin.util.fp;

@FunctionalInterface
public interface IntFunction<T> {

    T apply(int input);

    default <R> IntFunction<R> andThen(Function<T, R> f) {
        return i -> f.apply(apply(i));
    }

}
