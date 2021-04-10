package dev.kabin.util.shapes.primitive;

/**
 * Implements an <b>unmodifiable</b> view of a {@link RectInt}.
 */
public record RectIntView(RectInt data) implements RectInt {

    @Override
    public int getMinX() {
        return data.getMinX();
    }

    @Override
    public int getMaxX() {
        return data.getMaxX();
    }

    @Override
    public int getMinY() {
        return data.getMinY();
    }

    @Override
    public int getMaxY() {
        return data.getMaxY();
    }

    @Override
    public int getWidth() {
        return data.getWidth();
    }

    @Override
    public int getHeight() {
        return data.getHeight();
    }

    @Override
    public float getCenterX() {
        return data.getCenterX();
    }

    @Override
    public float getCenterY() {
        return data.getCenterY();
    }

    @Override
    public boolean contains(int x, int y) {
        return data.contains(x, y);
    }

    @Override
    public boolean meets(RectInt other) {
        return data.meets(other);
    }
}
