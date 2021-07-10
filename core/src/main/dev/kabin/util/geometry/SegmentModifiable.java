package dev.kabin.util.geometry;

import dev.kabin.util.geometry.points.PointFloatModifiable;

/**
 * A modifiable 1-simplex.
 */
public record SegmentModifiable(PointFloatModifiable start,
								PointFloatModifiable end) implements Segment, RigidTransformations {
	public SegmentModifiable {
		if (start.equals(end)) {
			throw new IllegalArgumentException("Start must not equal end: " + this);
		}
	}

	@SuppressWarnings("MethodDoesntCallSuperMethod")
	@Override
	public Segment clone() {
		return new SegmentModifiable(start.clone(), end.clone());
	}

	@Override
	public void translate(float deltaX, float deltaY) {
		start.translate(deltaX, deltaY);
		end.translate(deltaX, deltaY);
	}

	@Override
	public void rotate(float pivotX, float pivotY, double angleRad) {
		start.rotate(pivotX, pivotY, angleRad);
		end.rotate(pivotX, pivotY, angleRad);
	}
}
