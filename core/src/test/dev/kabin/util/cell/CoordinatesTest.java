package dev.kabin.util.cell;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CoordinatesTest {

    @Test
    void of() {
        Assertions.assertDoesNotThrow(() -> Coordinates.of(0, 0, 0.5f, 0.99f));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Coordinates.of(0, 0, 1.5f, 0.5f));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Coordinates.of(0, 0, -1.5f, 0.5f));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Coordinates.of(0, 0, 0.5f, 1.5f));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Coordinates.of(0, 0, 0.5f, -1.5f));
    }

    @Test
    void addX_int() {
        Coordinates coord = Coordinates.of(0, 0, 0.5f, 0.7f);
        coord.addX(1);
        Assertions.assertEquals(1, coord.getCellX());
    }

    @Test
    void addX_float_noOverFlow() {
        var coord = Coordinates.of(0, 0, 0.5f, 0.7f);
        coord.addX(0.25f);
        Assertions.assertEquals(0.75f, coord.getX());
    }

    @Test
    void addX_float_overFlow_positive() {
        var coord = Coordinates.of(0, 0, 0.5f, 0.7f);
        coord.addX(0.75f);
        Assertions.assertEquals(0.25f, coord.getX());
        Assertions.assertEquals(1, coord.getCellX());
    }

    @Test
    void addX_float_overFlow_negative() {
        var coord = Coordinates.of(0, 0, 0.5f, 0.7f);
        coord.addX(-0.75f);
        Assertions.assertEquals(0.75f, coord.getX());
        Assertions.assertEquals(-1, coord.getCellX());
    }

    @Test
    void addY_float_noOverFlow() {
        var coord = Coordinates.of(0, 0, 0.5f, 0.7f);
        coord.addY(0.25f);
        Assertions.assertEquals(0.95f, coord.getY());
    }

    @Test
    void addY_float_overFlow_positive() {
        var coord = Coordinates.of(0, 0, 0.5f, 0.7f);
        coord.addY(0.75f);
        Assertions.assertEquals(0.45f, coord.getY(), 0.0001f);
        Assertions.assertEquals(1, coord.getCellY());
    }

    @Test
    void addY_float_overFlow_negative() {
        var coord = Coordinates.of(0, 0, 0.5f, 0.7f);
        coord.addY(-0.75f);
        Assertions.assertEquals(0.95f, coord.getY());
        Assertions.assertEquals(-1, coord.getCellY());
    }

    @Test
    void addY() {
        var coord = Coordinates.of(0, 0, 0.5f, 0.7f);
        coord.addY(1);
        Assertions.assertEquals(1, coord.getCellY());
    }

}