package dev.kabin.util.geometry;

import dev.kabin.util.Functions;

public interface Circle extends FloatCoordinates {
    float r();

    default boolean contains(float x, float y) {
        return Functions.distance(x, y, x(), y()) < r();
    }

    default boolean contains(FloatCoordinates xy) {
        return contains(xy.x(), xy.y());
    }
}
