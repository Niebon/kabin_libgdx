package dev.kabin.util.pools.objectpool;

public class AlternatingPair<T> {

    private final T a;
    private final T b;

    // Reference to the last returned.
    private T lastReturned;

    public AlternatingPair(T a, T b) {
        this.a = a;
        this.b = b;
        lastReturned = a;
    }

    public T getNext() {
        return lastReturned = ((lastReturned == a) ? b : a);
    }

}
