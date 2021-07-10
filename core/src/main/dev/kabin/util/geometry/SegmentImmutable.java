package dev.kabin.util.geometry;

import dev.kabin.util.geometry.points.PointFloatImmutable;

/**
 * A modifiable 1-simplex.
 */
public record SegmentImmutable(PointFloatImmutable start, PointFloatImmutable end) implements Segment {

	public SegmentImmutable {
		if (start.equals(end)) {
			throw new IllegalArgumentException("Start must not equal end: " + this);
		}
	}

}
