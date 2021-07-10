package dev.kabin.util.geometry.points;

import dev.kabin.util.geometry.FloatCoordinates;

public interface PointFloat extends FloatCoordinates {

    static PointFloatImmutable immutable(PointFloat p) {
        return new PointFloatImmutable(p.x(), p.y());
    }

    static PointFloatModifiable modifiable(PointFloat p) {
        return new PointFloatModifiable(p.x(), p.y());
    }

    static PointFloatImmutable immutable(float x, float y) {
        return new PointFloatImmutable(x, y);
    }

    static PointFloatModifiable modifiable(float x, float y) {
        return new PointFloatModifiable(x, y);
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
