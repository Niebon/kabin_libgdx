package dev.kabin.util.pathfinding;

import dev.kabin.util.collections.LazyList;
import dev.kabin.util.graph.Node;

/**
 * Path data consists of a list of nodes that hold {@link IndexedRect} .
 * The nodes are indexed such that their index corresponds
 * to their index in the list.
 */
record PathData(LazyList<? extends Node<IndexedRect>> pathSegments) {


	/**
	 * @param pathSegments a lazy list of nodes of {@link IndexedRect}. The {@link IndexedRect#index()} must match
	 *                     the index in the given list.
	 * @throws IllegalArgumentException if the condition described on path segments does not hold.
	 */
	PathData {
		if (!pathSegments.allMatch((i, o) -> i == o.obj().index())) throw new IllegalArgumentException();
	}


}
