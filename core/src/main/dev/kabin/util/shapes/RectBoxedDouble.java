package dev.kabin.util.shapes;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * This class uses boxed doubles for numbers and should (if possible) be avoided in main thread calls.
 * For main method call, use {@link RectDouble}.
 */
public class RectBoxedDouble extends AbstractRectBoxed<Double> {

    public RectBoxedDouble(double x, double y, double width, double height) {
        if (width < 0 || height < 0) throw new IllegalArgumentException();
        setMinX(x);
        setMaxX(x + width);
        setMinY(y);
        setMaxY(y + height);
    }

    @Contract("_,_->this")
    public RectBoxedDouble translate(double dx, double dy) {
        setMinX(getMinX() + dx);
        setMaxX(getMaxX() + dx);
        setMinY(getMinY() + dy);
        setMaxY(getMaxY() + dy);
        return this;
    }

    @Override
    public double getCenterX() {
        return 0.5 * (getMaxX() + getMinX());
    }

    @Override
    public double getCenterY() {
        return 0.5 * (getMaxY() + getMinY());
    }

    @NotNull
    @Override
    public Double getWidth() {
        return getMaxX() - getMinX();
    }

    @NotNull
    @Override
    public Double getHeight() {
        return getMaxY() - getMinY();
    }

    public RectBoxedInt toRectInt(double scaleBy) {
        return new RectBoxedInt(
                (int) Math.round(getMinX() / scaleBy),
                (int) Math.round(getMinY() / scaleBy),
                (int) Math.round(getWidth() / scaleBy),
                (int) Math.round(getHeight() / scaleBy)
        );
    }


}
