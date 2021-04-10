package dev.kabin.util.shapes.primitive;

/**
 * A mutable rectangle with {@code int} coordinates, that can grow by adding points.
 * It grows to the smallest rectangle containing the added point, or stays the same size, if the added point
 * is already contained.
 */
public class GrowingRectInt extends MutableRectInt {

    /**
     * @param x      determines {@link #getMinX()}.
     * @param y      determines {@link #getMinY()}.
     * @param width  determines {@link #getWidth()}.
     * @param height determines {@link #getHeight()}.
     */
    public GrowingRectInt(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    /**
     * Two instances of {@link RectInt} are said to be <b>isomorphic</b> if
     * they share the same coordinates.
     * <p>
     * If this rectangle already contains the added point, then nothing happens.
     * Otherwise, this instance is modified so that it becomes isomorphic to the smallest
     * rectangle that contains the added point.
     *
     * @param x the horizontal coordinate of the added point.
     * @param y the vertical coordinate of the added point.
     */
    public void add(int x, int y) {
        setMinX(Math.min(x, getMinX()));
        setMaxX(Math.max(x, getMaxX()));
        setMinY(Math.min(y, getMinY()));
        setMaxY(Math.max(y, getMaxY()));
    }

}
