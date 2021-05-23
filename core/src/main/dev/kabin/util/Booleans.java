package dev.kabin.util;

public class Booleans {

    public static <T> T ternOp2(boolean condition1, boolean condition2, T tt, T tf, T ft, T ff) {
        if (condition1 && condition2) return tt;
        if (condition1) return tf;
        if (condition2) return ft;
        return ff;
    }

    public static int toInt(boolean b) {
        return b ? 1 : 0;
    }
}
