package dev.kabin.util.deltacomplexes;

import dev.kabin.util.HashCodeUtil;

/**
 * A modifiable 0-simplex.
 */
public final class Simplex0Impl implements Simplex0 {

    private float x, y;

    public Simplex0Impl(float x, float y) {
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
        Simplex0Impl simplex0 = (Simplex0Impl) o;
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
