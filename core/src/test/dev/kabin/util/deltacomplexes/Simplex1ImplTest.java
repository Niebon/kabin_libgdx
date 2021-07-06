package dev.kabin.util.deltacomplexes;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class Simplex1ImplTest {

	@Test
	void intersects_disjoint1() {
		var s1 = Simplex1.of(-1, 0, 1, 0);
		var s2 = Simplex1.of(2, 0, 3, 0);
		Assertions.assertFalse(s1.intersects(s2));
		Assertions.assertFalse(s2.intersects(s1));
	}

	@Test
	void intersects_disjoint2() {
		var s1 = Simplex1.of(0, -1, 0, 1);
		var s2 = Simplex1.of(0, 2, 0, 3);
		Assertions.assertFalse(s1.intersects(s2));
		Assertions.assertFalse(s2.intersects(s1));
	}

	@Test
	void intersects_sameSegment() {
		var s1 = Simplex1.of(-1, 0, 1, 0);
		var s2 = Simplex1.of(-1, 0, 1, 0);
		Assertions.assertTrue(s1.intersects(s2));
		Assertions.assertTrue(s2.intersects(s1));
	}

	@Test
	void intersects_orthogonalSegments() {
		var s1 = Simplex1.of(-1, 0, 1, 0);
		var s2 = Simplex1.of(0, -1, 0, 1);
		Assertions.assertTrue(s1.intersects(s2));
		Assertions.assertTrue(s2.intersects(s1));
	}

	@Test
	void intersects_parallelSegments_xAxis() {
		var s1 = Simplex1.of(-1, 0, 1, 0);
		var s2 = Simplex1.of(0, 0, 2, 0);
		Assertions.assertTrue(s1.intersects(s2));
		Assertions.assertTrue(s2.intersects(s1));
	}

	@Test
	void intersects_parallelSegments_yAxis() {
		var s1 = Simplex1.of(0, -1, 0, 1);
		var s2 = Simplex1.of(0, 0, 0, 2);
		Assertions.assertTrue(s1.intersects(s2));
		Assertions.assertTrue(s2.intersects(s1));
	}

	@Test
	void translate() {
		var s1 = Simplex1.of(0, -1, 0, 1);
		s1.translate(2, 1);
		Assertions.assertEquals(2, s1.start().x(), 0.0001f);
		Assertions.assertEquals(2, s1.end().x(), 0.0001f);
		Assertions.assertEquals(0, s1.start().y(), 0.0001f);
		Assertions.assertEquals(2, s1.end().y(), 0.0001f);
	}

	@Test
	void rotate() {
		var s1 = Simplex1.of(1, 0, 2, 0);
		s1.rotate(0, 0, 0.5 * Math.PI);
		Assertions.assertEquals(0f, s1.start().x(), 0.0001f);
		Assertions.assertEquals(0f, s1.end().x(), 0.0001f);
		Assertions.assertEquals(1f, s1.start().y(), 0.0001f);
		Assertions.assertEquals(2f, s1.end().y(), 0.0001f);

		s1.rotate(0, 0, 0.5 * Math.PI);
		Assertions.assertEquals(-1f, s1.start().x(), 0.0001f);
		Assertions.assertEquals(-2f, s1.end().x(), 0.0001f);
		Assertions.assertEquals(0f, s1.start().y(), 0.0001f);
		Assertions.assertEquals(0f, s1.end().y(), 0.0001f);
	}

	@Test
	void isJoined() {
		var s1 = Simplex1.of(0, 0, 1, 1);
		var t1 = Simplex1.of(1, 1, 2, 2);
		Assertions.assertTrue(s1.isJoined(t1));
		Assertions.assertFalse(t1.isJoined(s1));
	}

}