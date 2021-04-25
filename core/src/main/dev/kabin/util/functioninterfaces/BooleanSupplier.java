package dev.kabin.util.functioninterfaces;

@FunctionalInterface
public interface BooleanSupplier {

    boolean isTrue();

    default boolean isFalse() {
        return !isTrue();
    }

}
