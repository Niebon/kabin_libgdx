package dev.kabin.util.geometry.polygon;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Objects;

class PointImplTest {

	@Test
	void of() {
		var s0 = Point.of(1f, 0f);
		Assertions.assertEquals(1f, s0.x());
		Assertions.assertEquals(0f, s0.y());
	}

	@Test
	void setX() {
		var s0 = Point.of(1f, -1f);
		s0.setX(0f);
		Assertions.assertEquals(0f, s0.x());
	}

	@Test
	void setY() {
		var s0 = Point.of(1f, -1f);
		s0.setY(0f);
		Assertions.assertEquals(0f, s0.y());
	}

	@Test
	void translate() {
		var s0 = Point.of(1f, -1f);
		s0.setY(0f);
		Assertions.assertEquals(0f, s0.y());
	}

	@Test
	void rotate() {
		var s0 = Point.of(1f, 0);
		s0.rotate(0, 0, 0.5 * Math.PI);
		Assertions.assertEquals(0f, s0.x(), 0.0001f);
		Assertions.assertEquals(1f, s0.y(), 0.0001f);

		s0.rotate(0, 0, 0.5 * Math.PI);
		Assertions.assertEquals(-1f, s0.x(), 0.0001f);
		Assertions.assertEquals(0f, s0.y(), 0.0001f);
	}

	@Test
	void rotate_aboutPivotPointDifferentFromOrigin() {
		var s0 = Point.of(2f, 0);
		s0.rotate(1, 0, 0.5 * Math.PI);
		Assertions.assertEquals(1f, s0.x(), 0.0001f);
		Assertions.assertEquals(1f, s0.y(), 0.0001f);

		s0.rotate(1, 0, 0.5 * Math.PI);
		Assertions.assertEquals(0f, s0.x(), 0.0001f);
		Assertions.assertEquals(0f, s0.y(), 0.0001f);
	}

	@Test
	void testEquals() {
		Assertions.assertEquals(Point.of(0, 1), Point.of(0, 1));
	}

	@Test
	void testHashCode() {
		Assertions.assertEquals(Objects.hash(0f, 1f), Point.of(0, 1).hashCode());
	}

	@Test
	void testToString() {
		Assertions.assertEquals("Simplex0Impl{x=0.0, y=1.0}", Point.of(0, 1).toString());
	}
}