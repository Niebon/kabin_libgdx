package dev.kabin.utilities;

import dev.kabin.utilities.functioninterfaces.BiIntConsumer;

import java.util.function.IntConsumer;

public class Procedures {

    public static void forEachIntInRange(int from, int to, IntConsumer consumer) {
        for (int i = from; i < to; i++) {
            consumer.accept(i);
        }
    }

    public static void forEachIntPairIn(int imin, int imax, int jmin, int jmax, BiIntConsumer biIntConsumer) {
        for (int i = imin; i < imax; i++) {
            for (int j = jmin; j < jmax; j++) {
                biIntConsumer.accept(i, j);
            }
        }
    }

}
