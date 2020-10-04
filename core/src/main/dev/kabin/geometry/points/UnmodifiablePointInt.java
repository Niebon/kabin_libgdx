package dev.kabin.geometry.points;


/**
 * An unmodifiable implementation of {@link PrimitivePointInt}.
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
}
