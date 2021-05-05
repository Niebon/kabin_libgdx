package dev.kabin.util.lambdas;

@FunctionalInterface
public interface Function<T, R> {

    static <T> T identity(T input) {
        return input;
    }

    R apply(T input);


}
