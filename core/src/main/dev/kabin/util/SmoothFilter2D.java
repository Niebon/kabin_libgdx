package dev.kabin.util;

public class SmoothFilter2D {

    private final SmoothFilter x, y;

    public SmoothFilter2D(float alpha, float beta) {
        x = new SmoothFilter(alpha, beta);
        y = new SmoothFilter(alpha, beta);
    }

    public void appendSignalX(float x) {
        this.x.appendSignal(x);
    }

    public void appendSignalY(float y) {
        this.y.appendSignal(y);
    }

    public float x() {
        return x.get();
    }

    public float y() {
        return y.get();
    }
}
