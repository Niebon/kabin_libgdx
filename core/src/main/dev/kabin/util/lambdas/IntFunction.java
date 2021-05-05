package dev.kabin.util.lambdas;

@FunctionalInterface
public interface IntFunction<T> {

    T apply(int input);

    default <R> IntFunction<R> andThen(Function<T, R> f) {
        return i -> f.apply(apply(i));
    }

}
