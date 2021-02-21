package dev.kabin.util;

public class WeightedAverage2D {
    private final WeightedAverage x, y;

    public WeightedAverage2D(float alpha) {
        x = new WeightedAverage(alpha);
        y = new WeightedAverage(alpha);
    }

    public void appendSignalX(float x) {
        this.x.add(x);
    }

    public void appendSignalY(float y) {
        this.y.add(y);
    }

    public float x() {
        return x.get();
    }

    public float y() {
        return y.get();
    }
}
