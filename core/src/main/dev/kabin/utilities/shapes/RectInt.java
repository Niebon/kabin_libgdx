package dev.kabin.utilities.shapes;

import org.jetbrains.annotations.Contract;
import org.json.JSONObject;

import java.util.Objects;

/**
 * This class uses primitives instead of boxed values for doubles.
 * Preferably, use this class instead of the boxed version {@link RectBoxedDouble} if utilized in the main loop.
 * For the boxed version, see {@link RectBoxedDouble}.
 */
public class RectInt {

    private int minX, maxX, minY, maxY;

    public RectInt(int x, int y, int width, int height) {
        if (width < 0 || height < 0) throw new IllegalArgumentException();
        minX = x;
        minY = y;
        maxX = x + width;
        maxY = y + height;
    }

    public static RectInt centeredAt(int x, int y, int width, int height) {
        return new RectInt(x - Math.round(width * 0.5f), y - Math.round(height * 0.5f), width, height);
    }

    public int getWidth() {
        return maxX - minX;
    }

    public int getHeight() {
        return maxY - minY;
    }

    @Contract("_,_->this")
    public RectInt translate(int dx, int dy) {
        minX = minX + dx;
        minY = minY + dy;
        maxX = maxX + dx;
        maxY = maxY + dy;
        return this;
    }

    public float getCenterX() {
        return 0.5f * (minX + maxX);
    }

    public float getCenterY() {
        return 0.5f * (minY + maxY);
    }

    public boolean contains(int x, int y) {
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

    public int calculateArea() {
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
        RectInt that = (RectInt) o;
        return Objects.equals(minX, that.minX) &&
                Objects.equals(maxX, that.maxX) &&
                Objects.equals(minY, that.minY) &&
                Objects.equals(maxY, that.maxY);
    }

    @Override
    public int hashCode() {
        return Objects.hash(minX, maxX, minY, maxY);
    }

    public int getMinX() {
        return minX;
    }

    public void setMinX(int minX) {
        this.minX = minX;
    }

    public int getMaxX() {
        return maxX;
    }

    public void setMaxX(int maxX) {
        this.maxX = maxX;
    }

    public int getMinY() {
        return minY;
    }

    public void setMinY(int minY) {
        this.minY = minY;
    }

    public int getMaxY() {
        return maxY;
    }

    public void setMaxY(int maxY) {
        this.maxY = maxY;
    }

    public boolean meets(RectInt other) {
        return (maxX - other.minX > 0 &&
                minX - other.maxX < 0 &&
                maxY - other.minY > 0 &&
                minY - other.maxY < 0);
    }
}
