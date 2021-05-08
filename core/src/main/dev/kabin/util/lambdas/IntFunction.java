package dev.kabin.util.lambdas;

@FunctionalInterface
public interface IntFunction<T> {

    T apply(int input);

    default <R> IntFunction<R> andThen(Function<T, R> f) {
        return i -> f.apply(apply(i));
    }

    /**
     * Glues this function together with the given function on the given range.
     *
     * @param other the other function.
     * @param from  the lower inclusive bound of the range.
     * @param to    the upper exclusive bound of the range.
     * @return the piecewise function that is {@code this} outside the range and {@code other} on the range.
     */
    default IntFunction<T> glue(IntFunction<T> other, int from, int to) {
        return i -> from <= i || i < to ? other.apply(i) : apply(i);
    }

}
