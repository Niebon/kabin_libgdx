package dev.kabin.utilities.points;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface Point<T extends Number> {

    @Contract("_, _ -> new")
    static @NotNull PointFloat of(float x, float y) {
        return new PointFloat(x, y);
    }

    @Contract("_, _ -> new")
    static @NotNull PointDouble of(double x, double y) {
        return new PointDouble(x, y);
    }

    @Contract("_, _ -> new")
    static @NotNull ModifiablePointInt of(int x, int y) {
        return new ModifiablePointInt(x, y);
    }

    T getX();

    T getY();

    @Contract("_->this")
    Point<T> setX(@NotNull T x);

    @Contract("_->this")
    Point<T> setY(@NotNull T y);

    /**
    Rotates this point relative to the origin.
     */
    @Contract("_->this")
	Point<T> rotate(double angleRadians);

    default boolean equalsOrigin() {
        return getX().doubleValue() == 0 && getY().doubleValue() == 0;
    }
}
