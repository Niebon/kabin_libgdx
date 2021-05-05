package dev.kabin.util.lambdas;

@FunctionalInterface
public interface FloatSupplier {

    static float zero() {
        return 0;
    }

    float get();
}
