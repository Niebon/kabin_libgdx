package dev.kabin.util.geometry;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EdgeTest {

    @Test
    void equals() {
        Assertions.assertEquals(Edge.immutable(0, 0, 1, 0), Edge.immutable(0, 0, 1, 0));
        Assertions.assertEquals(Edge.modifiable(0, 0, 1, 0), Edge.immutable(0, 0, 1, 0));
        Assertions.assertEquals(Edge.immutable(0, 0, 1, 0), Edge.modifiable(0, 0, 1, 0));
        Assertions.assertEquals(Edge.modifiable(0, 0, 1, 0), Edge.modifiable(0, 0, 1, 0));
    }

}
