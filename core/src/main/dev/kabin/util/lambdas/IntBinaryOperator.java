package dev.kabin.util.lambdas;

/*
 * Javas implementation leads to silly warnings for parameter names if the provided parameter name does not
 * equal left/right.
 */
@FunctionalInterface
public
interface IntBinaryOperator {
    int apply(int x, int y);
}
