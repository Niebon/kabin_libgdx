package dev.kabin.utilities.collections;

import java.util.Arrays;

/**
 * A lightweight int to int function. Uses binary searches do to mappings, and creates no garbage,
 * except when increasing size past load capacity.
 */
public class IntToIntFunction {

    private int[] domain;
    private int[] coDomain;

    private int size;

    public IntToIntFunction() {
        this(8);
    }

    public IntToIntFunction(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("Capacity must be positive.");
        domain = new int[capacity];
        coDomain = new int[capacity];
    }

    public void put(int input, int output) {
        int searchResult = searchDomain(input);
        int insertIndex;
        if (0 <= searchResult && searchResult < size) {
            insertIndex = searchResult;
            coDomain[insertIndex] = output;
        }
        else {
            insertIndex = - searchResult - 1;
            int newSize = size + 1;
            ensureCapacity(newSize);
            shiftByOne(insertIndex);
            domain[insertIndex] = input;
            coDomain[insertIndex] = output;
            size = newSize;
        }
    }

    private void ensureCapacity(int upToIndex) {
        if (upToIndex >= domain.length) {
            final int[] newDomain = new int[2 * domain.length];
            final int[] newCoDomain = new int[2 * coDomain.length];
            for (int i = 0, n = size; i < n; i++) {
                newDomain[i] = domain[i];
                newCoDomain[i] = coDomain[i];
            }
            domain = newDomain;
            coDomain = newCoDomain;
        }
    }

    private void shiftByOne(int atIndex) {
        for (int i = size; i > atIndex; i--) {
            domain[i] = domain[i-1];
            coDomain[i] = coDomain[i-1];
        }
    }

    private int searchDomain(int val) {
        return Arrays.binarySearch(domain, 0, size, val);
    }

    public int eval(int input) {
        return coDomain[searchDomain(input)];
    }
}
