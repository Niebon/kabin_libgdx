package dev.kabin.util.functioninterfaces;

@FunctionalInterface
public interface FloatSupplier {

    private static float getZero() {
        return 0;
    }

    static FloatSupplier trivial(){
        return FloatSupplier::getZero;
    }

    float get();
}
