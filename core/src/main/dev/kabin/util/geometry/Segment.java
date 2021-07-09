package dev.kabin.util.geometry;

import dev.kabin.util.geometry.points.PointFloat;
import dev.kabin.util.helperinterfaces.FloatCoordinates;

/**
 * An oriented line segment with a start and an end.
 */
public interface Segment {

	/**
	 * A factory method for a line segment.
	 *
	 * @param startX the horizontal coordinate of the start point.
	 * @param startY the vertical coordinate of the start point.
	 * @param endX   the horizontal coordinate of the start point.
	 * @param endY   the vertical coordinate of the start point.
	 * @return a 1-simplex defined by the input.
	 */
	static Segment modifiable(float startX, float startY, float endX, float endY) {
		return new SegmentModifiable(PointFloat.modifiable(startX, startY), PointFloat.modifiable(endX, endY));
	}

	/**
	 * @return a deep clone of this segment.
	 */
	Segment clone();

	PointFloat start();

	PointFloat end();

	default float startX() {
		return start().x();
	}

	default float startY() {
		return start().y();
	}

	default float endX() {
		return end().x();
	}

	default float endY() {
		return end().y();
	}

	/**
	 * @return the slope, or {@code a} of the line {@code y = ax + b}
	 * determined by this segment.
	 */
	default float slope() {
		float dx = endX() - startX();
		float dy = endY() - startY();
		return dy / dx;
	}

	/**
	 * @return the constant term, or {@code b} of the line {@code y = ax + b}
	 * determined by this segment.
	 */
	default float constantTerm() {
		return startY() - slope() * startY();
	}

	default boolean intersects(Segment other) {
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

	void translate(float deltaX, float deltaY);

	void rotate(float pivotX, float pivotY, double angleRad);

	/**
	 * @param other the other simplex.
	 * @return true if the end of this simplex equals the start of the other simplex. Otherwise false.
	 */
	default boolean isJoined(Segment other) {
		return end().equals(other.start());
	}

	private boolean contains(FloatCoordinates p) {
		// Check whether p is on the line or not:
		return p.x() <= Math.max(startX(), endX()) && p.x() >= Math.min(startX(), endX()) &&
				p.y() <= Math.max(startY(), endY()) && p.y() >= Math.min(startY(), endY());
	}

	private int direction(FloatCoordinates a, FloatCoordinates b, FloatCoordinates c) {
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
