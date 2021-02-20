package dev.kabin.util.points;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@Deprecated(forRemoval = true)
public interface PointOld<T extends Number> {

    @Contract("_, _ -> new")
    static @NotNull PointFloatOld of(float x, float y) {
        return new PointFloatOld(x, y);
    }

    @Contract("_, _ -> new")
    static @NotNull PointOldDouble of(double x, double y) {
        return new PointOldDouble(x, y);
    }

    @Contract("_, _ -> new")
    static @NotNull ModifiablePointInt of(int x, int y) {
        return new ModifiablePointInt(x, y);
    }

    T getX();

    T getY();

    @Contract("_->this")
    PointOld<T> setX(@NotNull T x);

    @Contract("_->this")
    PointOld<T> setY(@NotNull T y);

    /**
    Rotates this point relative to the origin.
     */
    @Contract("_->this")
    PointOld<T> rotate(double angleRadians);

    default boolean equalsOrigin() {
        return getX().doubleValue() == 0 && getY().doubleValue() == 0;
    }
}
