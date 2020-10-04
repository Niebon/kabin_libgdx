package dev.kabin.geometry.points;

import dev.kabin.geometry.shapes.RectFloat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PointFloatTest {

    @Test
    public void creation() {
        RectFloat r = new RectFloat(0, 1, 2, 4);

        // Getters
        Assertions.assertEquals(0, r.getMinX());
        Assertions.assertEquals(1, r.getMinY());
        Assertions.assertEquals(2, r.getMaxX());
        Assertions.assertEquals(5, r.getMaxY());

        // Calculations
        Assertions.assertEquals(2, r.getWidth());
        Assertions.assertEquals(4, r.getHeight());
        Assertions.assertEquals(1.0, r.getCenterX());
        Assertions.assertEquals(3.0, r.getCenterY());
    }

    @Test
    public void translation() {
        RectFloat r = new RectFloat(0, 1, 2, 4);
        r.translate(1, 2);
        Assertions.assertEquals(1, r.getMinX());
        Assertions.assertEquals(3, r.getMinY());
        Assertions.assertEquals(3, r.getMaxX());
        Assertions.assertEquals(7, r.getMaxY());
    }

    @Test
    public void contains() {
        RectFloat r = new RectFloat(0, 1, 2, 4);
        Assertions.assertTrue(r.contains(1, 3));
        Assertions.assertFalse(r.contains(5, 10));
    }

    @Test
    public void meets() {
        RectFloat r1 = new RectFloat(0, 1, 2, 4);
        RectFloat r2 = new RectFloat(-5, -2, 6, 4);
        Assertions.assertTrue(r1.meets(r2));
    }
}