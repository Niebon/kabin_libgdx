package dev.kabin.util.lambdas;

@FunctionalInterface
public interface BiFunction<TR, TL, T> {

    @SuppressWarnings("unused")
    static <TR, TL, T> TL projectLeft(TL left, TR right) {
        return left;
    }

    @SuppressWarnings("unused")
    static <TR, TL, T> TR projectRight(TL left, TR right) {
        return right;
    }

    T apply(TR left, TL right);


}
