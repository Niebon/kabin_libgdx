package dev.kabin.geometry.shapes;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class AbstractRectBoxed<T extends Number & Comparable<T>> implements RectBoxed<T> {

    private T minX = null, maxX = null, minY = null, maxY = null;

    AbstractRectBoxed() {
        super();
    }

    AbstractRectBoxed(@NotNull AbstractRectBoxed<T> copy) {
        this.maxX = copy.maxX;
        this.minX = copy.minX;
        this.maxY = copy.maxY;
        this.minY = copy.minY;
    }

    @NotNull
    public T getMinX() {
        return minX;
    }

    public void setMinX(T minX) {
        this.minX = minX;
    }

    @NotNull
    public T getMaxX() {
        return maxX;
    }

    public void setMaxX(T maxX) {
        this.maxX = maxX;
    }

    @NotNull
    public T getMinY() {
        return minY;
    }

    public void setMinY(T minY) {
        this.minY = minY;
    }

    @NotNull
    public T getMaxY() {
        return maxY;
    }

    public void setMaxY(T maxY) {
        this.maxY = maxY;
    }

    @Override
    public String toString() {
        return "{" + "x:" + getMinX() + "," +
                "y:" + getMaxX() + "," +
                "width:" + getWidth() + "," +
                "height:" + getHeight() +"}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractRectBoxed<?> that = (AbstractRectBoxed<?>) o;
        return Objects.equals(minX, that.minX) &&
                Objects.equals(maxX, that.maxX) &&
                Objects.equals(minY, that.minY) &&
                Objects.equals(maxY, that.maxY);
    }

    @Override
    public int hashCode() {
        return Objects.hash(minX, maxX, minY, maxY);
    }
}