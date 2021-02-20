package dev.kabin.util.points;

public interface PointInt {

    static ImmutablePointInt immutablePointInt(int x, int y) {
        return new ImmutablePointInt(x, y);
    }

    static ModifiablePointInt modifiableOf(int x, int y) {
        return new ModifiablePointInt(x, y);
    }

    int x();

    int y();
}
