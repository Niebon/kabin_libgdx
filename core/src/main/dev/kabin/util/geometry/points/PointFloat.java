package dev.kabin.util.geometry.points;

import dev.kabin.util.geometry.FloatCoordinates;
import dev.kabin.util.lambdas.BiFloatFunction;
import dev.kabin.util.lambdas.FloatBinaryOperator;

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

    default PointFloat map(BiFloatFunction<FloatCoordinates> function) {
        var res = function.apply(x(), y());
        return immutable(res.x(), res.y());
    }

    default PointFloat map(FloatBinaryOperator x, FloatBinaryOperator y) {
        return immutable(x.apply(x(), y()), y.apply(x(), y()));
    }

    default PointFloat mapX(FloatBinaryOperator x) {
        return immutable(x.apply(x(), y()), y());
    }

    default PointFloat mapY(FloatBinaryOperator y) {
        return immutable(x(), y.apply(x(), y()));
    }

}
