package dev.kabin.util;

public class ExponentialSmoothener2D {

    private final ExponentialSmoothener x, y;

    public ExponentialSmoothener2D(float alpha, float initX, float initY) {
        x = new ExponentialSmoothener(alpha, initX);
        y = new ExponentialSmoothener(alpha, initY);
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
