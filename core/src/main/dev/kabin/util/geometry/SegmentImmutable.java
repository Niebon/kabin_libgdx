package dev.kabin.util.geometry;

import dev.kabin.util.geometry.points.ImmutablePointFloat;

/**
 * A modifiable 1-simplex.
 */
public record SegmentImmutable(ImmutablePointFloat start, ImmutablePointFloat end) implements Segment {
	public SegmentImmutable {
		if (start.equals(end)) {
			throw new IllegalArgumentException("Start must not equal end: " + this);
		}
	}

	@Override
	public void translate(float deltaX, float deltaY) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void rotate(float pivotX, float pivotY, double angleRad) {
		throw new UnsupportedOperationException();
	}


}
