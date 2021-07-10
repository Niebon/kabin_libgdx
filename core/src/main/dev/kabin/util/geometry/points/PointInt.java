package dev.kabin.util.geometry.points;

public interface PointInt {

    static PointIntImmutable immutable(int x, int y) {
        return new PointIntImmutable(x, y);
    }

    static PointIntModifiable modifiable(int x, int y) {
        return new PointIntModifiable(x, y);
    }

    int x();

    int y();


}
