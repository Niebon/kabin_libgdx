package dev.kabin.util.shapes;


import dev.kabin.util.HashCodeUtil;
import org.json.JSONObject;

import java.util.Objects;

/**
 * This class uses primitives instead of boxed values for doubles.
 * Preferably, use this class instead of the boxed version {@link RectBoxedDouble} if utilized in the main loop.
 * For the boxed version, see {@link RectBoxedDouble}.
 */
@Deprecated
public class RectDouble {

    private double minX, maxX, minY, maxY;

    public RectDouble(double x, double y, double width, double height) {
        if (width < 0 || height < 0) throw new IllegalArgumentException();
        minX = x;
        minY = y;
        maxX = x + width;
        maxY = y + height;
    }

    public double getWidth() {
        return maxX - minX;
    }

    public double getHeight() {
        return maxY - minY;
    }

    public RectDouble translate(double dx, double dy) {
        minX = minX + dx;
        minY = minY + dy;
        maxX = maxX + dx;
        maxY = maxY + dy;
        return this;
    }

    public double getCenterX() {
        return 0.5 * (minX + maxX);
    }

    public double getCenterY() {
        return 0.5 * (minY + maxY);
    }

    public boolean contains(double x, double y) {
        return minX <= x && x <= maxX && minY <= y && y <= maxY;
    }

    public JSONObject getJSONObject() {
        JSONObject o = new JSONObject();
        o.put("x", minX);
        o.put("y", minY);
        o.put("width", getWidth());
        o.put("height", getHeight());
        return o;
    }

    public double calculateArea() {
        return getWidth() * getHeight();
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
        RectDouble that = (RectDouble) o;
        return Objects.equals(minX, that.minX) &&
                Objects.equals(maxX, that.maxX) &&
                Objects.equals(minY, that.minY) &&
                Objects.equals(maxY, that.maxY);
    }

    @Override
    public int hashCode() {
        return HashCodeUtil.hashCode(Double.hashCode(minX), Double.hashCode(maxX), Double.hashCode(minY), Double.hashCode(maxY));
    }

    public double getMinX() {
        return minX;
    }

    public void setMinX(double minX) {
        this.minX = minX;
    }

    public double getMaxX() {
        return maxX;
    }

    public void setMaxX(double maxX) {
        this.maxX = maxX;
    }

    public double getMinY() {
        return minY;
    }

    public void setMinY(double minY) {
        this.minY = minY;
    }

    public double getMaxY() {
        return maxY;
    }

    public void setMaxY(double maxY) {
        this.maxY = maxY;
    }

    public boolean meets(RectDouble other) {
        return (maxX - other.minX > 0 &&
                minX - other.maxX < 0 &&
                maxY - other.minY > 0 &&
                minY - other.maxY < 0);
    }

}
