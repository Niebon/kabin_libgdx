package dev.kabin.util.fp;

@FunctionalInterface
public interface BooleanSupplier {

    boolean isTrue();

    default boolean isFalse() {
        return !isTrue();
    }

}
