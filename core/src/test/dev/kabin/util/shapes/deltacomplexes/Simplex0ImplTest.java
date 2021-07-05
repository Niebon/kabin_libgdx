package dev.kabin.util.shapes.deltacomplexes;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class Simplex0ImplTest {

	@Test
	void of() {
		var s0 = Simplex0Impl.of(1f, 0f);
		Assertions.assertEquals(1f, s0.x());
		Assertions.assertEquals(0f, s0.y());
	}

	@Test
	void setX() {
		var s0 = Simplex0Impl.of(1f, -1f);
		s0.setX(0f);
		Assertions.assertEquals(0f, s0.x());
	}

	@Test
	void setY() {
		var s0 = Simplex0Impl.of(1f, -1f);
		s0.setY(0f);
		Assertions.assertEquals(0f, s0.y());
	}

	@Test
	void translate() {
		var s0 = Simplex0Impl.of(1f, -1f);
		s0.setY(0f);
		Assertions.assertEquals(0f, s0.y());
	}

	@Test
	void rotate() {
		var s0 = Simplex0Impl.of(1f, 0);
		s0.rotate(0, 0, 0.5 * Math.PI);
		Assertions.assertEquals(0f, s0.x(), 0.0001f);
		Assertions.assertEquals(1f, s0.y(), 0.0001f);

		s0.rotate(0, 0, 0.5 * Math.PI);
		Assertions.assertEquals(-1f, s0.x(), 0.0001f);
		Assertions.assertEquals(0f, s0.y(), 0.0001f);
	}

	@Test
	void rotate_aboutPivotPointDifferentFromOrigin() {
		var s0 = Simplex0Impl.of(2f, 0);
		s0.rotate(1, 0, 0.5 * Math.PI);
		Assertions.assertEquals(1f, s0.x(), 0.0001f);
		Assertions.assertEquals(1f, s0.y(), 0.0001f);

		s0.rotate(1, 0, 0.5 * Math.PI);
		Assertions.assertEquals(0f, s0.x(), 0.0001f);
		Assertions.assertEquals(0f, s0.y(), 0.0001f);
	}

}