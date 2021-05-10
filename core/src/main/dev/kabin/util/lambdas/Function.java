package dev.kabin.util.lambdas;

@FunctionalInterface
public interface Function<T, R> {

    static <T> T identity(T input) {
        return input;
    }

    R apply(T input);

    default <T1> Function<T1, R> compose(Function<T1, T> g) {
        return t1 -> apply(g.apply(t1));
    }

    default <R1> Function<T, R1> andThen(Function<R, R1> g) {
        return t -> g.apply(apply(t));
    }

}
