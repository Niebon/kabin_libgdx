package dev.kabin.util.shapes.deltacomplexes;

/**
 * A modifiable 1-simplex.
 */
public record Simplex1Impl(Simplex0 start, Simplex0 end) implements Simplex1 {
	public static Simplex1 of(float startX, float startY, float endX, float endY) {
		return new Simplex1Impl(Simplex0Impl.of(startX, startY), Simplex0Impl.of(endX, endY));
	}
}
