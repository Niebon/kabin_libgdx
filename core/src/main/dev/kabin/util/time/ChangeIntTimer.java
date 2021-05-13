package dev.kabin.util.time;

public class ChangeIntTimer {

    private int value;
    private long timeCurrent;

    public int get() {
        return value;
    }

    public void set(int value) {
        this.timeCurrent = 0L;
        this.value = value;
    }

    public long getTimeOnCurrent() {
        return timeCurrent;
    }

}
