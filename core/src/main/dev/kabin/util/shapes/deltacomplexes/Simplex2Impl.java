package dev.kabin.util.shapes.deltacomplexes;

public record Simplex2Impl(Simplex1 e1, Simplex1 e2, Simplex1 e3) implements Simplex2 {

	public Simplex2Impl {
		if (!e1.isJoined(e2)) {
			throw new IllegalArgumentException("Edges of a %s and b %s are not properly joined.".formatted(e1, e2));
		}
		if (!e2.isJoined(e3)) {
			throw new IllegalArgumentException("Edges of b %s and c %s are not properly joined.".formatted(e2, e3));
		}
		if (!e3.isJoined(e1)) {
			throw new IllegalArgumentException("Edges of c %s and a %s are not properly joined.".formatted(e3, e1));
		}
	}

	@Override
	public void rotate(float pivotX, float pivotY, double angleRad) {
		e1().start().rotate(pivotX, pivotY, angleRad);
		e2().start().rotate(pivotX, pivotY, angleRad);
		e3().start().rotate(pivotX, pivotY, angleRad);
	}

	@Override
	public void translate(float deltaX, float deltaY) {
		e1().start().translate(deltaX, deltaY);
		e2().start().translate(deltaX, deltaY);
		e3().start().translate(deltaX, deltaY);
	}
}
