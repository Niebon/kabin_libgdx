package dev.kabin.util;

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
    public static <T> T drawUniform(@NotNull List<T> list, double prob) {
        return RANDOM.nextDouble() < prob ? list.get(RANDOM.nextInt(list.size())) : null;
    }

    /**
     * Get random item from list with uniform probability.
     */
    @Nullable
    public static <T> T drawUniform(@NotNull List<T> list) {
        int i = RANDOM.nextInt(list.size());
        return list.get(i);
    }
}
