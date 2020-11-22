package dev.kabin.utilities.points;

public interface PointInt {
    static UnmodifiablePointInt unmodifiableOf(int x, int y) {
        return new UnmodifiablePointInt(x, y);
    }

    static PointInt modifiableOf(int x, int y) {
        return new ModifiablePointInt(x, y);
    }

    int getX();

    int getY();
}
