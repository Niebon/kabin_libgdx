package dev.kabin.util.fp;

@FunctionalInterface
public interface BiIntToFloatFunction {
    float eval(int i, int j);
}
