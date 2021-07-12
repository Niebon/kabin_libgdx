package dev.kabin.util.geometry;

import dev.kabin.util.geometry.points.PointFloat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class TriangleModifiableTest {

    @Test
    void of() {
        var s2 = Triangle.modifiable(
                0, 0,
                0, 1,
                1, 1
        );
        Assertions.assertEquals(Triangle.modifiable(
                Edge.modifiable(0, 0, 0, 1),
                Edge.modifiable(0, 1, 1, 1),
                Edge.modifiable(1, 1, 0, 0)
        ), s2);
    }

    @Test
    void p1() {
        var s2 = Triangle.modifiable(
                0, 0,
                0, 1,
                1, 1
        );
        Assertions.assertEquals(PointFloat.modifiable(0, 0), s2.p1());
    }

    @Test
    void p2() {
        var s2 = Triangle.modifiable(
                0, 0,
                0, 1,
                1, 1
        );
        Assertions.assertEquals(PointFloat.modifiable(0, 1), s2.p2());
    }

    @Test
    void p3() {
        var s2 = Triangle.modifiable(
                0, 0,
                0, 1,
                1, 1
        );
        Assertions.assertEquals(PointFloat.modifiable(1, 1), s2.p3());
    }

    @Test
    void contains() {
        var s2 = Triangle.modifiable(
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
        var s2 = Triangle.modifiable(
                0, 0,
                0, 1,
                1, 1
        );
        Assertions.assertEquals(0.5f, s2.area(), 0.005f);
    }

    @Test
    void intersects_selfIntersecting() {
        var A = Triangle.modifiable(
                0, 0,
                0, 1,
                1, 1
        );
        var B = Triangle.modifiable(
                0, 0,
                0, 1,
                1, 1
        );
        Assertions.assertTrue(A.intersects(B));
    }

    @Test
    void intersects_completelyDisjoint() {
        var A = Triangle.modifiable(
                0, 0,
                0, 1,
                1, 1
        );
        var B = Triangle.modifiable(
                2, 2,
                2, 3,
                3, 3
        );
        Assertions.assertFalse(A.intersects(B));
    }

    @Test
    void rotate() {
        var s1 = Triangle.modifiable(
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
        var s1 = Triangle.modifiable(
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

    @Test
    void hasEdge() {
        var s1 = Triangle.modifiable(
                0, 0,
                1, 0,
                1, 1
        );
        Assertions.assertTrue(s1.hasEdge(Edge.immutable(0, 0, 1, 0)));
    }
}