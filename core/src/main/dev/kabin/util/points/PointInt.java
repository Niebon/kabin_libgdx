package dev.kabin.util.points;

public interface PointInt {

    static ImmutablePointInt immutable(int x, int y) {
        return new ImmutablePointInt(x, y);
    }

    static ModifiablePointInt modifiable(int x, int y) {
        return new ModifiablePointInt(x, y);
    }

    int x();

    int y();
}
