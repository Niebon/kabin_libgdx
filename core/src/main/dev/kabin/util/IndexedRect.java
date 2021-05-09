package dev.kabin.util;

import dev.kabin.util.shapes.primitive.RectInt;

public record IndexedRect(RectInt rect, int index, int connectedIndex) {
}
