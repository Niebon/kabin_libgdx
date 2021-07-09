package dev.kabin.util.geometry.points;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Objects;

class ModifiablePointTest {

	@Test
	void of() {
		var s0 = PointFloat.modifiable(1f, 0f);
		Assertions.assertEquals(1f, s0.x());
		Assertions.assertEquals(0f, s0.y());
	}

	@Test
	void setX() {
		var s0 = PointFloat.modifiable(1f, -1f);
		s0.setX(0f);
		Assertions.assertEquals(0f, s0.x());
	}

	@Test
	void setY() {
		var s0 = PointFloat.modifiable(1f, -1f);
		s0.setY(0f);
		Assertions.assertEquals(0f, s0.y());
	}

	@Test
	void translate() {
		var s0 = PointFloat.modifiable(1f, -1f);
		s0.setY(0f);
		Assertions.assertEquals(0f, s0.y());
	}

	@Test
	void rotate() {
		var s0 = PointFloat.modifiable(1f, 0);
		s0.rotate(0, 0, 0.5 * Math.PI);
		Assertions.assertEquals(0f, s0.x(), 0.0001f);
		Assertions.assertEquals(1f, s0.y(), 0.0001f);

		s0.rotate(0, 0, 0.5 * Math.PI);
		Assertions.assertEquals(-1f, s0.x(), 0.0001f);
		Assertions.assertEquals(0f, s0.y(), 0.0001f);
	}

	@Test
	void rotate_aboutPivotPointDifferentFromOrigin() {
		var s0 = PointFloat.modifiable(2f, 0);
		s0.rotate(1, 0, 0.5 * Math.PI);
		Assertions.assertEquals(1f, s0.x(), 0.0001f);
		Assertions.assertEquals(1f, s0.y(), 0.0001f);

		s0.rotate(1, 0, 0.5 * Math.PI);
		Assertions.assertEquals(0f, s0.x(), 0.0001f);
		Assertions.assertEquals(0f, s0.y(), 0.0001f);
	}

	@Test
	void testEquals() {
		Assertions.assertEquals(PointFloat.modifiable(0, 1), PointFloat.modifiable(0, 1));
	}

	@Test
	void testHashCode() {
		Assertions.assertEquals(Objects.hash(0f, 1f), PointFloat.modifiable(0, 1).hashCode());
	}

	@Test
	void testToString() {
		Assertions.assertEquals("PointFloat{x=0.0, y=1.0}", PointFloat.modifiable(0, 1).toString());
	}
}