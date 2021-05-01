package dev.kabin.util;

public record HalfOpenRectangle(HalfOpenInterval intervalX, HalfOpenInterval intervalY) {

    public static HalfOpenRectangle of(int minX, int maxX, int minY, int maxY) {
        return new HalfOpenRectangle(new HalfOpenInterval(minX, maxX), new HalfOpenInterval(minY, maxY));
    }

    public boolean contains(int x, int y) {
        return intervalX.contains(x) && intervalY.contains(y);
    }

}
