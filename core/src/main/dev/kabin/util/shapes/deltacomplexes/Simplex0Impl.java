package dev.kabin.util.shapes.deltacomplexes;

/**
 * A modifiable 0-simplex.
 */
public class Simplex0Impl {

    private float x, y;

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float x() {
        return x;
    }

    public float y() {
        return y;
    }

}
