package dev.kabin.util;

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
}