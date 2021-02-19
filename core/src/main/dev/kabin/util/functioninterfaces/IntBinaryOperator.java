package dev.kabin.util.functioninterfaces;

/*
 * Javas implementation leads to silly warnings for parameter names if the provided parameter name does not
 * equal left/right.
 */
@FunctionalInterface
public
interface IntBinaryOperator {
    int eval(int x, int y);
}
