package dev.kabin.utilities.functioninterfaces;

@FunctionalInterface
public
interface BiIntToDoubleFunction {
    BiIntToFloatFunction TRIVIAL = (i, j) -> 0f;
    double apply(int i, int j);
}
