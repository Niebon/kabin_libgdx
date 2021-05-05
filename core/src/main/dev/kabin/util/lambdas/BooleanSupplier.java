package dev.kabin.util.lambdas;

@FunctionalInterface
public interface BooleanSupplier {

    boolean isTrue();

    default boolean isFalse() {
        return !isTrue();
    }

}
