package dev.kabin.util;

public class SmoothFilter {

    private final float alpha, beta;
    private float valSmooth, lastAppendedVal;

    public SmoothFilter(float alpha, float beta) {
        this.alpha = alpha;
        this.beta = beta;
    }

    public void appendSignal(float val) {
        valSmooth = alpha * val + beta * lastAppendedVal;
        this.lastAppendedVal = val;
    }

    public float get() {
        return valSmooth;
    }

}
