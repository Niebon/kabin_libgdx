package dev.kabin.util.shapes;

import dev.kabin.util.points.PointOld;

@Deprecated
public interface Growing<T extends Number & Comparable<T>> {
    void add(PointOld<T> point);
}
