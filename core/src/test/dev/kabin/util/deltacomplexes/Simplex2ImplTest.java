package dev.kabin.util.deltacomplexes;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class Simplex2ImplTest {

	@Test
	void of() {
		var s2 = Simplex2.of(
				0, 0,
				0, 1,
				1, 1
		);
		Assertions.assertEquals(new Simplex2Impl(
				Simplex1.of(0, 0, 0, 1),
				Simplex1.of(0, 1, 1, 1),
				Simplex1.of(1, 1, 0, 0)
		), s2);
	}

	@Test
	void p1() {
		var s2 = Simplex2.of(
				0, 0,
				0, 1,
				1, 1
		);
		Assertions.assertEquals(Simplex0.of(0, 0), s2.p1());
	}

	@Test
	void p2() {
		var s2 = Simplex2.of(
				0, 0,
				0, 1,
				1, 1
		);
		Assertions.assertEquals(Simplex0.of(0, 1), s2.p2());
	}

	@Test
	void p3() {
		var s2 = Simplex2.of(
				0, 0,
				0, 1,
				1, 1
		);
		Assertions.assertEquals(Simplex0.of(1, 1), s2.p3());
	}

	@Test
	void contains() {
		var s2 = Simplex2.of(
				0, 0,
				0, 1,
				1, 1
		);
		Assertions.assertTrue(s2.contains(0.5f, 0.5f));
		Assertions.assertFalse(s2.contains(-2, 0));
		Assertions.assertFalse(s2.contains(0, -2));
		Assertions.assertFalse(s2.contains(2, 0));
		Assertions.assertFalse(s2.contains(0, 2));
		Assertions.assertFalse(s2.contains(2, 2));
		Assertions.assertFalse(s2.contains(-2, -2));
	}

	@Test
	void area() {
		var s2 = Simplex2.of(
				0, 0,
				0, 1,
				1, 1
		);
		Assertions.assertEquals(0.5f, s2.area(), 0.005f);
	}

	@Test
	void intersects_selfIntersecting() {
		var A = Simplex2.of(
				0, 0,
				0, 1,
				1, 1
		);
		var B = Simplex2.of(
				0, 0,
				0, 1,
				1, 1
		);
		Assertions.assertTrue(A.intersects(B));
	}

	@Test
	void intersects_completelyDisjoint() {
		var A = Simplex2.of(
				0, 0,
				0, 1,
				1, 1
		);
		var B = Simplex2.of(
				2, 2,
				2, 3,
				3, 3
		);
		Assertions.assertFalse(A.intersects(B));
	}

	@Test
	void rotate() {
		var s1 = Simplex2.of(
				0, 0,
				1, 0,
				1, 1
		);
		s1.rotate(0, 0, 0.5 * Math.PI);
		Assertions.assertEquals(0, s1.p1().x(), 0.005f);
		Assertions.assertEquals(0, s1.p1().y(), 0.005f);
		Assertions.assertEquals(0, s1.p2().x(), 0.005f);
		Assertions.assertEquals(1, s1.p2().y(), 0.005f);
		Assertions.assertEquals(-1, s1.p3().x(), 0.005f);
		Assertions.assertEquals(1, s1.p3().y(), 0.005f);
	}

	@Test
	void translate() {
		var s1 = Simplex2.of(
				0, 0,
				1, 0,
				1, 1
		);
		s1.translate(1, 2);
		Assertions.assertEquals(1, s1.p1().x(), 0.005f);
		Assertions.assertEquals(2, s1.p1().y(), 0.005f);
		Assertions.assertEquals(2, s1.p2().x(), 0.005f);
		Assertions.assertEquals(2, s1.p2().y(), 0.005f);
		Assertions.assertEquals(2, s1.p3().x(), 0.005f);
		Assertions.assertEquals(3, s1.p3().y(), 0.005f);
	}

	@Test
	void e1() {
	}

	@Test
	void e2() {
	}

	@Test
	void e3() {
	}
}