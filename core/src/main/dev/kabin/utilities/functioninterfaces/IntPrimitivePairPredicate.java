package dev.kabin.utilities.functioninterfaces;

@FunctionalInterface
public interface IntPrimitivePairPredicate {
    boolean eval(int x, int y);
}
