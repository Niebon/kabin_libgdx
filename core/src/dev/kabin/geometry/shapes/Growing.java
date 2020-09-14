package dev.kabin.geometry.shapes;

import dev.kabin.geometry.points.Point;

public interface Growing<T extends Number & Comparable<T>> {
    void add(Point<T> point);
}
