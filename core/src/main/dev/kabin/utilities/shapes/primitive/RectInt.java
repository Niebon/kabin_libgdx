package dev.kabin.utilities.shapes.primitive;

public interface RectInt {
    int getMinX();
    int getMaxX();
    int getMinY();
    int getMaxY();
    int getWidth();
    int getHeight();
    float getCenterX();
    float getCenterY();
    boolean contains(int x, int y);
    boolean meets(RectInt other);
}
