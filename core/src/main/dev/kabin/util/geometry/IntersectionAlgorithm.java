package dev.kabin.util.geometry;


/**
 * An instance of this class can store a point of intersection of two line segments.
 * These are stored into temp variables that can be accessed by getters.
 * So this class is intend for reuse.
 */
public final class IntersectionAlgorithm {

	private float xTemp = Float.NaN, yTemp = Float.NaN;

	public void accept(Segment segment1, Segment segment2) {
		if (segment1.intersects(segment2)) {

			// Line segment1 represented as a1x + b1y = c1
			float a1 = segment1.end().y() - segment1.start().y();
			float b1 = segment1.start().x() - segment1.end().x();
			float c1 = a1 * segment1.start().x() + b1 * segment1.start().y();

			// Line segment2 represented as a2x + b2y = c2
			float a2 = segment2.end().y() - segment2.start().y();
			float b2 = segment2.start().x() - segment2.end().x();
			float c2 = a2 * segment1.start().x() + b2 * segment1.start().y();

			float determinant = a1 * b2 - a2 * b1;

			if (determinant == 0) {
				// The lines are parallel. This is simplified
				// by returning a pair of FLT_MAX
				xTemp = Float.NaN;
				yTemp = Float.NaN;
			} else {
				xTemp = (b2 * c1 - b1 * c2) / determinant;
				yTemp = (a1 * c2 - a2 * c1) / determinant;
			}
		} else {
			xTemp = Float.NaN;
			yTemp = Float.NaN;
		}
	}

	public float getIntersectionX() {
		return xTemp;
	}

	public float getIntersectionY() {
		return yTemp;
	}

}
