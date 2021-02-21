package dev.kabin.util;

public class ExponentialSmoothener {

    private final float alpha, beta;
    private float valSmooth, lastAppendedVal;

    public ExponentialSmoothener(float alpha, float init) {
        this.alpha = alpha;
        this.beta = 1 - alpha;
        lastAppendedVal = init;
    }

    public void appendSignal(float val) {
        valSmooth = alpha * val + beta * lastAppendedVal;
        this.lastAppendedVal = val;
    }

    public float get() {
        return valSmooth;
    }

}
