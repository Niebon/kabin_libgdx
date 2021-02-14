package dev.kabin.utilities.points;


import dev.kabin.utilities.HashCodeUtil;

/**
 * An unmodifiable implementation of {@link PointInt}.
 * Also implements hash {@link #equals(Object)} and {@link #hashCode()}, so it should be safe to
 * use as hash-map keys.
 */
public final class UnmodifiablePointInt implements PointInt {

    final int x, y;

    public UnmodifiablePointInt(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    final public int x() {
        return x;
    }

    @Override
    final public int y() {
        return y;
    }

    @Override
    final public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PointInt)) return false;
        PointInt that = (PointInt) o;
        return x == that.x() &&
                y == that.y();
    }

    @Override
    final public int hashCode() {
        return HashCodeUtil.hashCode(x, y);
    }

	@Override
	public String toString() {
		return "PointInt{" +
				"x=" + x +
				", y=" + y +
				'}';
	}
}
