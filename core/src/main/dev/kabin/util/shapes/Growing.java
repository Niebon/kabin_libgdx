package dev.kabin.util.shapes;

import dev.kabin.util.points.Point;

public interface Growing<T extends Number & Comparable<T>> {
    void add(Point<T> point);
}
