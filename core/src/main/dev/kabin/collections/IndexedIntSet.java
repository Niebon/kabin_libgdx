package dev.kabin.collections;

import java.util.Arrays;

/**
 * Keeps a set of {@code int} in an underlying array. Holds at most one of each integer.
 * Has a getter such that
 * <p> {@link #get(int) get(index)} for all {@code index} from 0 up to size (but not including) returns
 * all integers this list stores in increasing order. This can be utilized to iterate
 * over a set of {@code int}s without the use of an iterator object.
 */
public class IndexedIntSet {

    private static final int INITIAL_SIZE = 8;

    private int[] underlyingArray;
    private int size = 0;

    public IndexedIntSet() {
        underlyingArray = new int[INITIAL_SIZE];
    }

    IndexedIntSet(int... values) {
        underlyingArray = values;
        size = underlyingArray.length;
    }

    public void add(int val) {

        int result = Arrays.binarySearch(underlyingArray, 0, size, val);

        if (result < 0) {

            if (size == underlyingArray.length) {
                int[] copy = new int[underlyingArray.length * 2];
                System.arraycopy(underlyingArray, 0, copy, 0, size);
                underlyingArray = copy;
            }

            /*
            Start at the end of the array and update each index i as i -> i + 1.
             */
            int indexOfVal = -result - 1;
            if (size - indexOfVal >= 0) {
                System.arraycopy(underlyingArray, indexOfVal, underlyingArray, indexOfVal + 1, size - indexOfVal);
            }
            underlyingArray[indexOfVal] = val;

            size++;
        }
    }

    int get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException();
        }
        return underlyingArray[index];
    }

    public int[] toArray() {
        return underlyingArray;
    }

    public int size() {
        return size;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        int outSize = Math.min(100, size);
        for (int i = 0; i < outSize; i++) {
            sb.append(underlyingArray[i]);
        }
        boolean printDots = outSize < size;
        if (printDots) sb.append("...");
        sb.append(']');
        return sb.toString();
    }
}
