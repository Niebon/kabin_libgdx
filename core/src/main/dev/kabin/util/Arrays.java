package dev.kabin.util;

import dev.kabin.util.lambdas.IntFunction;

import java.util.Comparator;

public class Arrays {

	public static <T> void quickSort(int[] array, int low, int high, IntFunction<T> toObj, Comparator<T> comparator) {
		if (low < high) {

			// pi is partitioning index, arr[p]
			// is now at right place
			int pi = partition(array, low, high, toObj, comparator);

			// Separately sort elements before
			// partition and after partition
			quickSort(array, low, pi - 1, toObj, comparator);
			quickSort(array, pi + 1, high, toObj, comparator);
		}
	}

	public static <T> void quickSort(int[] array, IntFunction<T> toObj, Comparator<T> comparator) {
		quickSort(array, 0, array.length - 1, toObj, comparator);
	}

	private static void swap(int[] arr, int i, int j) {
		int temp = arr[i];
		arr[i] = arr[j];
		arr[j] = temp;
	}

	private static <T> int partition(int[] arr, int low, int high, IntFunction<T> toObj, Comparator<T> comparator) {

		// pivot
		T pivot = toObj.apply(arr[high]);

		// Index of smaller element and
		// indicates the right position
		// of pivot found so far
		int i = (low - 1);

		for (int j = low; j <= high - 1; j++) {

			// If current element is smaller
			// than the pivot
			if (comparator.compare(toObj.apply(arr[j]), pivot) < 0) {

				// Increment index of
				// smaller element
				i++;
				swap(arr, i, j);
			}
		}
		swap(arr, i + 1, high);
		return (i + 1);
	}
}
