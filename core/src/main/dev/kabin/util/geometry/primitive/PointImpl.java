package dev.kabin.util.geometry.primitive;

import dev.kabin.util.HashCodeUtil;
import dev.kabin.util.geometry.polygon.Point;

/**
 * A modifiable 0-simplex.
 */
public final class PointImpl implements Point {

    private float x, y;

    public PointImpl(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    @Override
    public float x() {
        return x;
    }

    @Override
    public float y() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PointImpl simplex0 = (PointImpl) o;
        return Float.compare(simplex0.x, x) == 0 && Float.compare(simplex0.y, y) == 0;
    }

    @Override
    public int hashCode() {
        return HashCodeUtil.hashCode(Float.hashCode(x), Float.hashCode(y));
    }

    @Override
    public String toString() {
        return "Simplex0Impl{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }

}
