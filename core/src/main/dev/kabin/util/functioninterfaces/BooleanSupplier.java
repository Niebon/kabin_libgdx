package dev.kabin.util.functioninterfaces;

public interface BooleanSupplier {

    boolean isTrue();

    default boolean isFalse() {
        return !isTrue();
    }

}
