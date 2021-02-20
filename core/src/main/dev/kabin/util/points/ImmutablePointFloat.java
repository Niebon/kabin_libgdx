package dev.kabin.util.points;


import dev.kabin.util.HashCodeUtil;

/**
 * An immutable implementation of {@link PointInt}.
 * Also implements hash {@link #equals(Object)} and {@link #hashCode()}, so it should be safe to
 * use as hash-map keys.
 */
public final class ImmutablePointFloat implements PointFloat {

    final float x, y;

    public ImmutablePointFloat(float x, float y) {
        this.x = x;
        this.y = y;
    }

    @Override
    final public float x() {
        return x;
    }

    @Override
    final public float y() {
        return y;
    }

    @Override
    final public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PointFloat)) return false;
        PointFloat that = (PointFloat) o;
        return x == that.x() &&
                y == that.y();
    }

    @Override
    final public int hashCode() {
        return HashCodeUtil.hashCode(Float.hashCode(x), Float.hashCode(y));
    }

	@Override
	public String toString() {
		return "PointFloat{" +
				"x=" + x +
				", y=" + y +
				'}';
	}
}
