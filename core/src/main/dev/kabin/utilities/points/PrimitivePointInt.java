package dev.kabin.utilities.points;

public interface PrimitivePointInt {
	int getX();
	int getY();

	static UnmodifiablePointInt newUnmodifiable(int x, int y) {
		return new UnmodifiablePointInt(x, y);
	}

	static PrimitivePointInt newModifiable(int x, int y) {
		return new PointInt(x, y);
	}
}
