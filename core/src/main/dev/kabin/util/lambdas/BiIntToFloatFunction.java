package dev.kabin.util.lambdas;

@FunctionalInterface
public interface BiIntToFloatFunction {
    float eval(int i, int j);
}
