package dev.kabin.util.fp;

@FunctionalInterface
public interface Function<T, R> {


    static <T> T identity(T input) {
        return input;
    }

    R apply(T input);


}
