package dev.kabin.util.geometry.points;


import dev.kabin.util.HashCodeUtil;

/**
 * An immutable implementation of {@link PointInt}.
 * Also implements hash {@link #equals(Object)} and {@link #hashCode()}, so it should be safe to
 * use as hash-map keys.
 */
public record PointFloatImmutable(float x, float y) implements PointFloat {

    @Override
    final public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PointFloat that)) return false;
        return x == that.x() &&
                y == that.y();
    }

    @Override
    final public int hashCode() {
        return HashCodeUtil.hashCode(Float.hashCode(x), Float.hashCode(y));
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public PointFloatImmutable clone() {
        return new PointFloatImmutable(x, y);
    }
}