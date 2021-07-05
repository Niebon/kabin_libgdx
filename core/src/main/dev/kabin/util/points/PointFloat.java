package dev.kabin.util.points;

import dev.kabin.util.helperinterfaces.FloatCoordinates;
import org.jetbrains.annotations.Contract;

public interface PointFloat extends FloatCoordinates {

    static ImmutablePointFloat immutable(float x, float y) {
        return new ImmutablePointFloat(x, y);
    }

    static ModifiablePointFloat modifiableOf(float x, float y) {
        return new ModifiablePointFloat(x, y);
    }

    @Contract("_->new")
    default PointFloat scaleBy(float scale) {
        return immutable(x() * scale, y() * scale);
    }

    default PointInt toPointInt() {
        return PointInt.immutable(Math.round(x()), Math.round(y()));
    }

}
