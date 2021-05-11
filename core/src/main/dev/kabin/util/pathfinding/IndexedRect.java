package dev.kabin.util.pathfinding;

import dev.kabin.util.shapes.primitive.RectInt;

/**
 * A rect identified by an index, and a connected index.
 * Two rects are <b>path connected</b> if their connected index agrees.
 */
public record IndexedRect(RectInt rect, int index, int connectedIndex) {
}
