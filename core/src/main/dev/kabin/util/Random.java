package dev.kabin.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Random {

    private static final java.util.Random RANDOM = new java.util.Random();

    private Random() {
    }

    /**
     * Get random item from list with Probability.
     */
    @Nullable
    public static <T> T maybeDrawUniformWithProbability(@NotNull List<T> list, double prob) {
        return RANDOM.nextDouble() < prob ? drawUniform(list) : null;
    }

    /**
     * Get random item from list with uniform probability.
     */
    @NotNull
    public static <T> T drawUniform(@NotNull List<@NotNull T> list) {
        return list.get(RANDOM.nextInt(list.size()));
    }

}
