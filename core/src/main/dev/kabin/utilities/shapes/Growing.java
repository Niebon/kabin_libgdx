package dev.kabin.utilities.shapes;

import dev.kabin.utilities.points.Point;

public interface Growing<T extends Number & Comparable<T>> {
    void add(Point<T> point);
}
