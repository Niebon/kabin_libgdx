package dev.kabin.util.shapes;

import dev.kabin.util.points.Point;
import org.jetbrains.annotations.NotNull;

public abstract class GrowingRectBoxed<T extends Number & Comparable<T>> extends AbstractRectBoxed<T> implements Growing<T> {

    public GrowingRectBoxed() {
        super();
    }

    public GrowingRectBoxed(AbstractRectBoxed<T> copy) {
        super(copy);
    }

    @Override
    public double getCenterX() {
        return 0.5 * (getMaxX().doubleValue() + getMinX().doubleValue());
    }

    @Override
    public double getCenterY() {
        return 0.5 * (getMaxY().doubleValue() + getMinY().doubleValue());
    }

    @NotNull
    @Override
    public abstract T getWidth();

    @NotNull
    @Override
    public abstract T getHeight();

    @Override
    public void add(@NotNull Point<T> point) {
        T x = point.getX();
        T y = point.getY();
        T minX = getMinX(), maxX = getMaxX(), minY = getMinY(), maxY = getMaxY();
        setMinX(x.compareTo(minX) < 0 ? x : minX);
        setMaxX(x.compareTo(maxX) > 0 ? x : maxX);
        setMinY(y.compareTo(minY) < 0 ? y : minY);
        setMaxY(y.compareTo(maxY) > 0 ? y : maxY);
    }


}
