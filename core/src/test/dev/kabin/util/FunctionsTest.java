package dev.kabin.util;

import dev.kabin.util.events.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class FunctionsTest {

    @Test
    void snapToGrid() {
        int partition = 2;
        Assertions.assertEquals(0, Functions.snapToGrid(1.99f, partition));
        Assertions.assertEquals(2, Functions.snapToGrid(2.1f, partition));
        Assertions.assertEquals(2, Functions.snapToGrid(3, partition));
        Assertions.assertEquals(2, Functions.snapToGrid(3.9f, partition));
        Assertions.assertEquals(4, Functions.snapToGrid(4f, partition));
        Assertions.assertEquals(-2, Functions.snapToGrid(-0.5f, partition));
        Assertions.assertEquals(0, Functions.snapToGrid(0f, partition));
        Assertions.assertEquals(-2, Functions.snapToGrid(-0.1f, partition));
    }

    @Test
    void concat() {
        Assertions.assertEquals(List.of("a", "b", "c"), Lists.concat(List.of("a", "b"), "c"));
    }

    @Test
    void get() {
        Assertions.assertEquals(Booleans.ternOp2(true, true, "tt", "tf", "ft", "ff"), "tt");
        Assertions.assertEquals(Booleans.ternOp2(true, false, "tt", "tf", "ft", "ff"), "tf");
        Assertions.assertEquals(Booleans.ternOp2(false, true, "tt", "tf", "ft", "ff"), "ft");
        Assertions.assertEquals(Booleans.ternOp2(false, false, "tt", "tf", "ft", "ff"), "ff");
    }

    @Test
    void findAngleRad() {
        Assertions.assertEquals(0, Functions.findAngleRad(1, 0));
        Assertions.assertEquals(Math.PI / 4, Functions.findAngleRad(1, 1));
        Assertions.assertEquals(Math.PI / 2, Functions.findAngleRad(0, 1));
        Assertions.assertEquals(Math.PI * 3 / 4, Functions.findAngleRad(-1, 1));
        Assertions.assertEquals(Math.PI, Functions.findAngleRad(-1, 0));
        Assertions.assertEquals(Math.PI * 5 / 4, Functions.findAngleRad(-1, -1));
        Assertions.assertEquals(Math.PI * 3 / 2, Functions.findAngleRad(0, -1));
        Assertions.assertEquals(Math.PI * 7 / 4, Functions.findAngleRad(1, -1));
    }
}