package dev.kabin.util.geometry.points;

import dev.kabin.util.helperinterfaces.FloatCoordinates;

public interface PointFloat extends FloatCoordinates {

    static ImmutablePointFloat immutable(float x, float y) {
        return new ImmutablePointFloat(x, y);
    }

    static ModifiablePointFloat modifiable(float x, float y) {
        return new ModifiablePointFloat(x, y);
    }

    default PointFloat scaleBy(float scale) {
        return immutable(x() * scale, y() * scale);
    }

    default PointInt toPointInt() {
        return PointInt.immutable(Math.round(x()), Math.round(y()));
    }

}
