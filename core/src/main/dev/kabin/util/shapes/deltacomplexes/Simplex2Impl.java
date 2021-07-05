package dev.kabin.util.shapes.deltacomplexes;

public record Simplex2Impl(Simplex1 a, Simplex1 b, Simplex1 c) implements Simplex2 {

	public Simplex2Impl {
		if (!a.isJoined(b)) {
			throw new IllegalArgumentException("Edges of a %s and b %s are not properly joined.".formatted(a, b));
		}
		if (!b.isJoined(c)) {
			throw new IllegalArgumentException("Edges of b %s and c %s are not properly joined.".formatted(b, c));
		}
		if (!c.isJoined(a)) {
			throw new IllegalArgumentException("Edges of c %s and a %s are not properly joined.".formatted(c, a));
		}
	}

}
