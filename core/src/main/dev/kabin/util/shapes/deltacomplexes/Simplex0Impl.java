package dev.kabin.util.shapes.deltacomplexes;

import dev.kabin.util.points.ModifiablePointFloat;

/**
 * A modifiable 0-simplex.
 */
public record Simplex0Impl(ModifiablePointFloat point) implements Simplex0 {

    /**
     * A factory constructor.
     *
     * @param x horizontal coordinate.
     * @param y vertical coordinate.
     * @return a 0 simplex with coordinates (0,0).
     */
    public static Simplex0 of(float x, float y) {
        return new Simplex0Impl(new ModifiablePointFloat(x, y));
    }

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
