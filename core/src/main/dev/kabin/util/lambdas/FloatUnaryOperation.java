package dev.kabin.util.lambdas;

@FunctionalInterface
public
interface FloatUnaryOperation {
    float eval(float val);

    static float identity(float val) {
        return val;
    }
}
