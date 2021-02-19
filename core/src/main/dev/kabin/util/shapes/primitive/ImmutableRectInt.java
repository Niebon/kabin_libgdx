package dev.kabin.util.shapes.primitive;

import dev.kabin.util.HashCodeUtil;

import java.util.Objects;

public class ImmutableRectInt implements RectInt{

    private final int minX;
    private final int minY;
    private final int maxX;
    private final int maxY;

    public ImmutableRectInt(int x, int y, int width, int height) {
        if (width < 0 || height < 0) throw new IllegalArgumentException();
        minX = x;
        minY = y;
        maxX = x + width;
        maxY = y + height;
    }

    @Override
    public int getMinX() {
        return minX;
    }

    @Override
    public int getMaxX() {
        return maxX;
    }

    @Override
    public int getMinY() {
        return minY;
    }

    @Override
    public int getMaxY() {
        return maxY;
    }

    @Override
    public int getWidth() {
        return maxX - minX;
    }

    @Override
    public int getHeight() {
        return maxY - minY;
    }

    @Override
    public float getCenterX() {
        return 0.5f * (minX + maxX);
    }

    @Override
    public float getCenterY() {
        return 0.5f * (minY + maxY);
    }

    @Override
    public boolean contains(int x, int y) {
        return minX <= x && x <= maxX && minY <= y && y <= maxY;
    }

    @Override
    public boolean meets(RectInt other) {
        return (maxX - other.getMinX() > 0 &&
                minX - other.getMaxX() < 0 &&
                maxY - other.getMinY() > 0 &&
                minY - other.getMaxY() < 0);
    }

    @Override
    public String toString() {
        return "{" + "x:" + minX + "," +
                "y:" + minY + "," +
                "width:" + getWidth() + "," +
                "height:" + getHeight() + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImmutableRectInt that = (ImmutableRectInt) o;
        return Objects.equals(minX, that.minX) &&
                Objects.equals(maxX, that.maxX) &&
                Objects.equals(minY, that.minY) &&
                Objects.equals(maxY, that.maxY);
    }

    @Override
    public int hashCode() {
        return HashCodeUtil.hashCode(minX, maxX, minY, maxY);
    }
}
