package dev.kabin.util.deltacomplexes;

/**
 * A modifiable 1-simplex.
 */
public record Simplex1Impl(Simplex0 start, Simplex0 end) implements Simplex1 {
	public Simplex1Impl {
		if (start.equals(end)) {
			throw new IllegalArgumentException("Start must not equal end: " + this);
		}
	}
}
