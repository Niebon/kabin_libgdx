package dev.kabin.util.pathfinding;

import dev.kabin.util.IndexedRect;
import dev.kabin.util.collections.LazyList;
import dev.kabin.util.graph.Node;

public record PathData(LazyList<? extends Node<IndexedRect>> pathSegments) {
}
