package dev.kabin.utilities.shapes;


import dev.kabin.utilities.HashCodeUtil;
import org.json.JSONObject;

import java.util.Objects;

/**
 * This class uses primitives instead of boxed values for floats.
 */
public class RectFloat {

    private float minX, maxX, minY, maxY;

    public RectFloat(float x, float y, float width, float height) {
        if (width < 0 || height < 0) throw new IllegalArgumentException();
        minX = x;
        minY = y;
        maxX = x + width;
        maxY = y + height;
    }

    public static RectFloat centeredAt(float x, float y, float width, float height) {
        return new RectFloat(x - width * 0.5f, y - height * 0.5f, width, height);
    }

    public float getWidth() {
        return maxX - minX;
    }

    public float getHeight() {
        return maxY - minY;
    }

    public RectFloat translate(float dx, float dy) {
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

    public boolean contains(float x, float y) {
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

    public float calculateArea() {
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
        RectFloat that = (RectFloat) o;
        return minX == that.minX &&
                maxX == that.maxX &&
                minY == that.minY &&
                maxY == that.maxY;
    }

    @Override
    public int hashCode() {
        return HashCodeUtil.hashCode(Float.hashCode(minX), Float.hashCode(maxX), Float.hashCode(minY), Float.hashCode(maxY));
    }

    public float getMinX() {
        return minX;
    }

    public void setMinX(float minX) {
        this.minX = minX;
    }

    public float getMaxX() {
        return maxX;
    }

    public void setMaxX(float maxX) {
        this.maxX = maxX;
    }

    public float getMinY() {
        return minY;
    }

    public void setMinY(float minY) {
        this.minY = minY;
    }

    public float getMaxY() {
        return maxY;
    }

    public void setMaxY(float maxY) {
        this.maxY = maxY;
    }

    public boolean meets(RectFloat other) {
        return (maxX - other.minX > 0 &&
                minX - other.maxX < 0 &&
                maxY - other.minY > 0 &&
                minY - other.maxY < 0);
    }

}
