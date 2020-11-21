package dev.kabin.utilities.shapes;

import dev.kabin.utilities.points.Point;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public interface RectBoxed<T extends Number & Comparable<T>> {

    default boolean meets(@NotNull AbstractRectBoxed<T> other) {
        return (getMaxX().compareTo(other.getMinX()) > 0 &&
                getMinX().compareTo(other.getMaxX()) < 0 &&
                getMaxY().compareTo(other.getMinY()) > 0 &&
                getMinY().compareTo(other.getMaxY()) < 0);
    }

    @NotNull T getMinX();

    void setMinX(T minX);

    @NotNull T getMaxX();

    void setMaxX(T maxX);

    @NotNull T getMinY();

    void setMinY(T minY);

    @NotNull T getMaxY();

    void setMaxY(T maxY);

    @NotNull T getWidth();

    @NotNull T getHeight();

    default double getCenterX() {
        return 0.5 * (getMinX().doubleValue() + getMaxX().doubleValue());
    }

    default double getCenterY() {
        return 0.5 * (getMinY().doubleValue() + getMaxY().doubleValue());
    }

    default boolean contains(@NotNull Point<T> point) {
        final double
                x = point.getX().doubleValue(),
                y = point.getY().doubleValue();
        return getMinX().doubleValue() <= x && x <= getMaxX().doubleValue() && getMinY().doubleValue() <= y
                && y <= getMaxY().doubleValue();
    }

    default boolean contains(@NotNull T x, @NotNull T y) {
        double x_ = x.doubleValue();
        double y_ = y.doubleValue();
        return getMinX().doubleValue() <= x_ && x_ <= getMaxX().doubleValue() && getMinY().doubleValue() <= y_
                && y_ <= getMaxY().doubleValue();
    }

    default boolean contains(double x, double y) {
        return getMinX().doubleValue() <= x && x <= getMaxX().doubleValue() && getMinY().doubleValue() <= y
                && y <= getMaxY().doubleValue();
    }

    default JSONObject getJSONObject() {
        JSONObject o = new JSONObject();
        o.put("x", getMinX());
        o.put("y", getMaxX());
        o.put("width", getWidth());
        o.put("height", getHeight());
        return o;
    }

    default double calculateArea() {
        return getWidth().doubleValue() * getHeight().doubleValue();
    }

}