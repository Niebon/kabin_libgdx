package dev.kabin.util;


public record HalfOpenIntInterval(int min, int max) {

    public boolean contains(int value) {
        return min <= value && value < max;
    }

}
