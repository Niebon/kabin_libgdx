package dev.kabin.util;

import dev.kabin.util.lambdas.BiIntConsumer;
import dev.kabin.util.shapes.primitive.MutableRectInt;

import java.util.function.IntConsumer;

/**
 * Helper procedures to iterate over integers without wrapper objects.
 */
public class Procedures {

    public static void forEachIntInRange(int from, int to, IntConsumer consumer) {
        for (int i = from; i < to; i++) {
            consumer.accept(i);
        }
    }

    public static void forEachIntPairIn(MutableRectInt rect, BiIntConsumer biIntConsumer) {
        forEachIntPairIn(rect.getMinX(), rect.getMaxX(), rect.getMinY(), rect.getMaxY(), biIntConsumer);
    }

    public static void forEachIntPairIn(int imin, int imax, int jmin, int jmax, BiIntConsumer biIntConsumer) {
        for (int i = imin; i < imax; i++) {
            for (int j = jmin; j < jmax; j++) {
                biIntConsumer.accept(i, j);
            }
        }
    }

}
