package dev.kabin.util.lambdas;

public interface Projection<L, R> {

    static <L, R> L left(L l, R r) {
        return l;
    }

    static <L, R> R right(L l, R r) {
        return r;
    }

    default L applyLeft(L l, R r) {
        return l;
    }

    default R applyRight(L l, R r) {
        return r;
    }
}
