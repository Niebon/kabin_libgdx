package dev.kabin.utilities.functioninterfaces;

@FunctionalInterface
public
interface FloatUnaryOperation {
    FloatUnaryOperation TRIVIAL = f -> f;
    float eval(float val);
}
