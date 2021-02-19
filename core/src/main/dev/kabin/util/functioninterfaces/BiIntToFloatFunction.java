package dev.kabin.util.functioninterfaces;

@FunctionalInterface
public
interface BiIntToFloatFunction {
    BiIntToFloatFunction TRIVIAL = (i, j) -> 0f;
    float eval(int i, int j);
}
