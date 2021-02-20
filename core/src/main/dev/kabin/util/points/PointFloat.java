package dev.kabin.util.points;

import org.jetbrains.annotations.Contract;

public interface PointFloat {

    static ImmutablePointFloat immutablePointFloat(float x, float y) {
        return new ImmutablePointFloat(x, y);
    }

    static ModifiablePointFloat modifiableOf(float x, float y) {
        return new ModifiablePointFloat(x, y);
    }

    float x();

    float y();

    @Contract("_->new")
    default PointFloat scaleBy(float scale) {
        return immutablePointFloat(x() * scale, y() * scale);
    }

    default PointInt toPointInt() {
        return PointInt.immutable(Math.round(x()), Math.round(y()));
    }

}
