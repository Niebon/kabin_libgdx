package dev.kabin.util;


public record HalfOpenInterval(int min, int max) {

    public boolean contains(int value) {
        return min <= value && value < max;
    }

}
