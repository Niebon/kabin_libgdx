package dev.kabin.util.graph;

public class NoSuchChildException extends IndexOutOfBoundsException {
    public NoSuchChildException(int index) {
        super(index);
    }
}
