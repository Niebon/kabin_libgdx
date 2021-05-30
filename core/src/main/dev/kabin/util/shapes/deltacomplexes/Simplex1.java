package dev.kabin.util.shapes.deltacomplexes;

public interface Simplex1 {

    Simplex0 start();

    Simplex0 end();

    default boolean intersects(Simplex1 other) {
        if (Math.max(start().x(), end().x()) < Math.min(other.start().x(), other.end().x())) {
            return false;
        }
        return false;
    }
}
