package dev.kabin.util;

public record HalfOpenIntRectangle(HalfOpenIntInterval intervalX, HalfOpenIntInterval intervalY) {

    public static HalfOpenIntRectangle of(int minX, int maxX, int minY, int maxY) {
        return new HalfOpenIntRectangle(new HalfOpenIntInterval(minX, maxX), new HalfOpenIntInterval(minY, maxY));
    }

    public boolean contains(int x, int y) {
        return intervalX.contains(x) && intervalY.contains(y);
    }

}
