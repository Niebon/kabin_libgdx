package dev.kabin.util.functioninterfaces;

@FunctionalInterface
public
interface BiIntToDoubleFunction {
    BiIntToFloatFunction TRIVIAL = (i, j) -> 0f;
    double apply(int i, int j);
}
