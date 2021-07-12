package dev.kabin.util.geometry;

import dev.kabin.util.HashCodeUtil;
import dev.kabin.util.geometry.points.PointFloatImmutable;

/**
 * A modifiable 1-simplex.
 */
public record EdgeImmutable(PointFloatImmutable start, PointFloatImmutable end) implements Edge {

	public EdgeImmutable {
		if (start.equals(end)) {
			throw new IllegalArgumentException("Start must not equal end: " + this);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if ((!(o instanceof Edge that))) return false;
		return start.equals(that.start()) && end.equals(that.end());
	}

	@Override
	public int hashCode() {
		return HashCodeUtil.hashCode(start.hashCode(), end.hashCode());
	}
}
