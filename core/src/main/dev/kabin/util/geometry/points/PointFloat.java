package dev.kabin.util.geometry.points;

import dev.kabin.util.helperinterfaces.FloatCoordinates;

public interface PointFloat extends FloatCoordinates {

    static ImmutablePointFloat immutable(PointFloat p) {
        return new ImmutablePointFloat(p.x(), p.y());
    }

    static ModifiablePointFloat modifiable(PointFloat p) {
        return new ModifiablePointFloat(p.x(), p.y());
    }

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

    /**
     * @return a deep clone of this point.
     */
    PointFloat clone();

}
