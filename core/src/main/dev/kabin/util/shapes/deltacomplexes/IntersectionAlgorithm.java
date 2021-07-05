package dev.kabin.util.shapes.deltacomplexes;


public final class IntersectionAlgorithm {

	private float xTemp = Float.NaN, yTemp = Float.NaN;

	public void accept(Simplex1 s1, Simplex1 s2) {
		if (s1.intersects(s2)) {

			// Line AB represented as a1x + b1y = c1
			float a1 = s1.end().y() - s1.start().y();
			float b1 = s1.start().x() - s1.end().x();
			float c1 = a1 * s1.start().x() + b1 * s1.start().y();

			// Line CD represented as a2x + b2y = c2
			float a2 = s2.end().y() - s2.start().y();
			float b2 = s2.start().x() - s2.end().x();
			float c2 = a2 * s1.start().x() + b2 * s1.start().y();

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
