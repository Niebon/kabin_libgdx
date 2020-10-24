package dev.kabin.utilities;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public class Statistics {

    public static final Random RANDOM = new Random();

    /**
     * Get random item from list with Probability.
     */
    @Nullable
    public static Object drawUniform(@NotNull List<?> list, double prob) {
        int i = RANDOM.nextInt(list.size());
        return RANDOM.nextDouble() < prob ? list.get(i) : null;
    }

    /**
     * Get random item from list with uniform probability.
     */
    public static Object drawUniform(@NotNull List<?> list) {
        int i = RANDOM.nextInt(list.size());
        return list.get(i);
    }
}
