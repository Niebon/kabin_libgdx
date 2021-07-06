package dev.kabin.util.deltacomplexes;

/**
 * An oriented 1-Simplex with a start and an end.
 */
public interface Simplex1 {

	/**
	 * A factory method for 1-simplex.
	 *
	 * @param startX the horizontal coordinate of the start point.
	 * @param startY the vertical coordinate of the start point.
	 * @param endX   the horizontal coordinate of the start point.
	 * @param endY   the vertical coordinate of the start point.
	 * @return a 1-simplex defined by the input.
	 */
	static Simplex1 of(float startX, float startY, float endX, float endY) {
		return new Simplex1Impl(Simplex0.of(startX, startY), Simplex0.of(endX, endY));
	}

	Simplex0 start();

	Simplex0 end();

	default boolean intersects(Simplex1 other) {
		// Four direction for two lines and points of other line.
		int dir1 = direction(start(), end(), other.start());
		int dir2 = direction(start(), end(), other.end());
		int dir3 = direction(other.start(), other.end(), start());
		int dir4 = direction(other.start(), other.end(), end());

		// Intersecting check:
		if (dir1 != dir2 && dir3 != dir4)
			return true;

		// When p2 of line2 are on the line1:
		if (dir1 == 0 && contains(other.start()))
			return true;

		// When p1 of line2 are on the line1:
		if (dir2 == 0 && contains(other.end()))
			return true;

		// When p2 of line1 are on the line2:
		if (dir3 == 0 && other.contains(start()))
			return true;

		// When p1 of line1 are on the line2:
		return dir4 == 0 && other.contains(end());
	}

	default void translate(float deltaX, float deltaY) {
		start().translate(deltaX, deltaY);
		end().translate(deltaX, deltaY);
	}

	default void rotate(float pivotX, float pivotY, double angleRad) {
		start().rotate(pivotX, pivotY, angleRad);
		end().rotate(pivotX, pivotY, angleRad);
	}

	/**
	 * @param other the other simplex.
	 * @return true if the end of this simplex equals the start of the other simplex. Otherwise false.
	 */
	default boolean isJoined(Simplex1 other) {
		return end().equals(other.start());
	}

	default boolean contains(Simplex0 p) {
		// Check whether p is on the line or not:
		return p.x() <= Math.max(start().x(), end().x()) && p.x() >= Math.min(start().x(), end().x()) &&
				p.y() <= Math.max(start().y(), end().y()) && p.y() >= Math.min(start().y(), end().y());
	}

	private int direction(Simplex0 a, Simplex0 b, Simplex0 c) {
		float val = (b.y() - a.y()) * (c.x() - b.x()) - (b.x() - a.x()) * (c.y() - b.y());
		if (val == 0)
			// Co-linear
			return 0;
		else if (val < 0)
			// Anti-clockwise direction
			return 2;
		// Clockwise direction
		return 1;
	}

}
