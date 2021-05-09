package dev.kabin.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ArraysTest {

	@Test
	void quickSort1() {
		int[] ints = {1, 2, 3, 4, 5, 6};
		Arrays.quickSort(ints, 0, ints.length - 1, i -> i, (i, j) -> Integer.compare(j, i));
		Assertions.assertArrayEquals(new int[]{6, 5, 4, 3, 2, 1}, ints);
	}

	@Test
	void quickSort2() {
		int[] ints = {1, 6, 3, 4, 5, 2};
		Arrays.quickSort(ints, 0, ints.length - 1, i -> i, (i, j) -> Integer.compare(j, i));
		Assertions.assertArrayEquals(new int[]{6, 5, 4, 3, 2, 1}, ints);
	}

}