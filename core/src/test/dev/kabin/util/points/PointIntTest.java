package dev.kabin.util.points;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Random;

class PointIntTest {

    @SuppressWarnings("RedundantCast")
    @Test
    void testEqualsAndHashCodes() {
        for (int i = 0; i < 100_000; i++) {
            int x = new Random().nextInt();
            int y = new Random().nextInt();
            Assertions.assertEquals((PointInt) PointInt.modifiable(x, y), (PointInt) PointInt.immutable(x, y));
            Assertions.assertEquals(PointInt.modifiable(x, y).hashCode(), PointInt.immutable(x, y).hashCode());
        }
    }
}