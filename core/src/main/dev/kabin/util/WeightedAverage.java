package dev.kabin.util;

public class WeightedAverage {

    private final float alpha;
    private float weightedAvg;

    public WeightedAverage(float alpha) {
        this.alpha = alpha;
    }

    public void add(float val) {
        weightedAvg = weightedAvg + (val - weightedAvg) * alpha;
    }

    float get() {
        return weightedAvg;
    }

}
