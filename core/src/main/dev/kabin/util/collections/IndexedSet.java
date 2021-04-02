package dev.kabin.util.collections;

import java.util.Arrays;
import java.util.Comparator;

/**
 * A set implementation for objects which implement {@link Id}. This set only accepts new elements in the
 * sense that their id does not equal the id of any of the elements already present in the set.
 * <p>
 * The interface {@link Id} is used to gain canonical getters on a set like structure in the sense that the call
 * <pre>
 *    {@code indexedSet.get(i)}
 * </pre>
 * returns the element of this set whose id is ordered at position i in the natural order on {@code int}s,
 * when considering the set of {@code int}s induced by the elements in this set.
 * <p>
 * For example, given a -> 4, b -> 10 and c -> 11 then the above call with i = 1 would return 10.
 */
public class IndexedSet<Param extends Id> {

    private static final int INITIAL_SIZE = 8;

    private int[] underlyingIntArray;
    private Object[] underlyingObjectArray;
    private int size = 0;

    public IndexedSet() {
        underlyingIntArray = new int[INITIAL_SIZE];
        underlyingObjectArray = new Object[INITIAL_SIZE];
    }

    @SafeVarargs
    public IndexedSet(Param... values) {
        underlyingObjectArray = values;
        Arrays.sort(values, Comparator.comparingInt(Id::getId));
        underlyingIntArray = new int[values.length];
        for (int i = 0; i < values.length; i++) {
            //noinspection unchecked
            underlyingIntArray[i] = ((Param) underlyingObjectArray[i]).getId();
        }
        size = underlyingObjectArray.length;
    }

    public boolean contains(Param val) {
        return Arrays.binarySearch(underlyingIntArray, 0, size, val.getId()) >= 0;
    }

    public boolean add(Param val) {

        int idOfVal = val.getId();
        int result = Arrays.binarySearch(underlyingIntArray, 0, size, idOfVal);

        if (result < 0) {

            if (size == underlyingIntArray.length) {
                int[] copyOfUnderlyingIntArray = new int[underlyingIntArray.length * 2];
                Object[] copyOfUnderlyingObjectArray = new Object[underlyingIntArray.length * 2];
                System.arraycopy(underlyingIntArray, 0, copyOfUnderlyingIntArray, 0, size);
                //noinspection ManualArrayCopy
                for (int i = 0; i < size; i++) {
                    copyOfUnderlyingObjectArray[i] = underlyingObjectArray[i];
                }
                underlyingIntArray = copyOfUnderlyingIntArray;
                underlyingObjectArray = copyOfUnderlyingObjectArray;
            }


            /*
            Start at the end of the array and update each index i as i -> i + 1.
             */
            int indexOfVal = -result - 1;
            if (size - indexOfVal >= 0) {
                System.arraycopy(underlyingIntArray, indexOfVal, underlyingIntArray, indexOfVal + 1, size - indexOfVal);
                //noinspection ManualArrayCopy
                for (int i = size - 1; i >= indexOfVal; i--) {
                    underlyingObjectArray[i + 1] = underlyingObjectArray[i];
                }
            }
            underlyingIntArray[indexOfVal] = idOfVal;
            underlyingObjectArray[indexOfVal] = val;

            size++;
            return true;
        } else return false;
    }

    public Param get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException();
        }
        //noinspection unchecked
        return (Param) underlyingObjectArray[index];
    }

    public int size() {
        return size;
    }

    public void clear() {
        size = 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        int outSize = Math.min(100, size);
        for (int i = 0; i < outSize; i++) {
            sb.append(underlyingObjectArray[i]);
        }
        boolean printDots = outSize < size;
        if (printDots) sb.append("...");
        sb.append(']');
        return sb.toString();
    }
}
