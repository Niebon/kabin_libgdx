package dev.kabin.util.shapes.deltacomplexes;

import dev.kabin.util.points.ModifiablePointFloat;

/**
 * A modifiable 0-simplex, implemented by delegation on a {@link ModifiablePointFloat}.
 */
public record Simplex0Impl(ModifiablePointFloat point) implements Simplex0 {

    public void setX(float x) {
        point.setX(x);
    }

    public void setY(float y) {
        point.setY(y);
    }

    @Override
    public float x() {
        return point.x();
    }

    @Override
    public float y() {
        return point.y();
    }

}
