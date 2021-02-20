package dev.kabin.util.points;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Random;

class PointIntTest {

    @Test
    void testEqualsAndHashCodes() {
        for (int i = 0; i < 100_000; i++) {
            int x = new Random().nextInt();
            int y = new Random().nextInt();
            Assertions.assertEquals(PointInt.modifiableOf(x, y), PointInt.immutablePointInt(x, y));
            Assertions.assertEquals(PointInt.modifiableOf(x, y).hashCode(), PointInt.immutablePointInt(x, y).hashCode());
        }
    }
}