package dev.kabin.util.functioninterfaces;

@FunctionalInterface
public
interface FloatUnaryOperation {
    FloatUnaryOperation TRIVIAL = f -> f;
    float eval(float val);
}
