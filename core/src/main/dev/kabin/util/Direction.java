package dev.kabin.util;

public enum Direction {
    LEFT, RIGHT, NONE;

    public int toInt(){
        return switch (this) {
            case LEFT -> -1;
            case RIGHT -> 1;
            case NONE -> 0;
        };
    }

    public static Direction valueOf(double d) {
        if (d > 0) {
            return Direction.RIGHT;
        }
        else if (d < 0) {
            return Direction.LEFT;
        }
        else return Direction.NONE;
    }
}