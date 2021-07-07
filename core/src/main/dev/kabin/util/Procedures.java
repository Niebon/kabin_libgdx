package dev.kabin.util;

import dev.kabin.util.lambdas.BiIntConsumer;

/**
 * Helper procedures to iterate over integers without wrapper objects.
 */
public class Procedures {

    public static void forEachIntPairIn(int imin, int imax, int jmin, int jmax, BiIntConsumer biIntConsumer) {
        for (int i = imin; i < imax; i++) {
            for (int j = jmin; j < jmax; j++) {
                biIntConsumer.accept(i, j);
            }
        }
    }

}
