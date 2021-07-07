package dev.kabin.util.geometry.points;


import dev.kabin.util.HashCodeUtil;

/**
 * An immutable implementation of {@link PointInt}.
 * Also implements hash {@link #equals(Object)} and {@link #hashCode()}, so it should be safe to
 * use as hash-map keys.
 */
public record ImmutablePointInt(int x, int y) implements PointInt {

    @Override
    final public int hashCode() {
        return HashCodeUtil.hashCode(x, y);
    }

}
