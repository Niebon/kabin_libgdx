package dev.kabin.utilities.functioninterfaces;

/*
 * Javas implementation leads to silly warnings for parameter names if the provided parameter name does not
 * equal left/right.
 */
@FunctionalInterface
public
interface IntPrimitiveBinaryOperator {
    int apply(int x, int y);
}
