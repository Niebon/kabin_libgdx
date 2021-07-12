package dev.kabin.util.geometry;

public record TriangleModifiable(EdgeModifiable e1, EdgeModifiable e2,
                                 EdgeModifiable e3) implements Triangle, RigidTransformations {

    public TriangleModifiable {
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
	public void rotate(double angleRad) {
		rotate(0, 0, angleRad);
	}

	@Override
	public void translate(float deltaX, float deltaY) {
		e1().start().translate(deltaX, deltaY);
		e2().start().translate(deltaX, deltaY);
		e3().start().translate(deltaX, deltaY);
	}

}
