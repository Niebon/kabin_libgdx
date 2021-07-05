package dev.kabin.util.shapes.deltacomplexes;

public interface Simplex2 {

	Simplex1 a();

	Simplex1 b();

	Simplex1 c();

	default void rotate(float pivotX, float pivotY, double angleRad) {
		a().rotate(pivotX, pivotY, angleRad);
		b().rotate(pivotX, pivotY, angleRad);
		c().rotate(pivotX, pivotY, angleRad);
	}

	default void translate(float deltaX, float deltaY) {
		a().translate(deltaX, deltaY);
		b().translate(deltaX, deltaY);
		c().translate(deltaX, deltaY);
	}

}
