package dev.kabin.util.points;

public interface PointFloat {

    static ImmutablePointFloat immutablePointFloat(float x, float y) {
        return new ImmutablePointFloat(x, y);
    }

    static ModifiablePointFloat modifiableOf(float x, float y) {
        return new ModifiablePointFloat(x, y);
    }

    float x();

    float y();
}
