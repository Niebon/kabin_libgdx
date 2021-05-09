package dev.kabin.util;

import dev.kabin.util.graph.Node;

import java.util.ArrayList;

public record PathData(ArrayList<? extends Node<IndexedRect>> pathSegments) {
}
