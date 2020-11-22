package dev.kabin.utilities.points;


import java.util.Objects;

/**
 * An unmodifiable implementation of {@link PrimitivePointInt}.
 * Also implements hash {@link #equals(Object)} and {@link #hashCode()}, so it should be safe to
 * use as hash-map keys.
 */
public class UnmodifiablePointInt implements PrimitivePointInt {

	final int x, y;

	public UnmodifiablePointInt(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof PrimitivePointInt)) return false;
		PrimitivePointInt that = (PrimitivePointInt) o;
		return x == that.getX() &&
				y == that.getY();
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, y);
	}
}
