package dev.kabin.util.geometry.polygon;

/**
 * A modifiable 1-simplex.
 */
public record SegmentImpl(Point start, Point end) implements Segment {
	public SegmentImpl {
		if (start.equals(end)) {
			throw new IllegalArgumentException("Start must not equal end: " + this);
		}
	}
}
