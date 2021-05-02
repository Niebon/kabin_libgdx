package dev.kabin.util.fp;

@FunctionalInterface
public interface FloatSupplier {

    static float zero() {
        return 0;
    }

    float get();
}
