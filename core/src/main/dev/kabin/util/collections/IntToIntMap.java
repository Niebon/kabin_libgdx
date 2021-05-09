package dev.kabin.util.collections;

import java.util.Arrays;

/**
 * A lightweight int to int function. Uses binary searches do to mappings, and creates no garbage,
 * except when increasing size past load capacity.
 */
public class IntToIntMap {

    private int[] keySet;
    private int[] valueSet;

    private int size;

    public IntToIntMap() {
        this(8);
    }

    public IntToIntMap(int capacity) {
        if (capacity < 0) throw new IllegalArgumentException("Capacity must be non-negative.");
        keySet = new int[capacity];
        valueSet = new int[capacity];
    }

    public void put(int key, int value) {
        final int searchResult = searchKeySet(key);
        final int insertIndex;
        if (0 <= searchResult && searchResult < size) {
            insertIndex = searchResult;
            valueSet[insertIndex] = value;
        } else {
            insertIndex = -searchResult - 1;
            final int newSize = size + 1;
            ensureCapacity(newSize);
            shiftByOne(insertIndex);
            keySet[insertIndex] = key;
            valueSet[insertIndex] = value;
            size = newSize;
        }
    }

    private void ensureCapacity(int upToIndex) {
        final int domainLength = keySet.length;
        if (upToIndex >= domainLength) {
            final int[] newDomain = new int[2 * domainLength];
            final int[] newCoDomain = new int[2 * domainLength];
            for (int i = 0, n = size; i < n; i++) {
                newDomain[i] = keySet[i];
                newCoDomain[i] = valueSet[i];
            }
            keySet = newDomain;
            valueSet = newCoDomain;
        }
    }

    private void shiftByOne(int atIndex) {
        for (int i = size; i > atIndex; i--) {
            keySet[i] = keySet[i - 1];
            valueSet[i] = valueSet[i - 1];
        }
    }

    private int searchKeySet(int key) {
        return Arrays.binarySearch(keySet, 0, size, key);
    }

    public int get(int key) {
        return valueSet[searchKeySet(key)];
    }

    public boolean containsKey(int key) {
        return Arrays.binarySearch(keySet, 0, size, key) > 0;
    }
}
