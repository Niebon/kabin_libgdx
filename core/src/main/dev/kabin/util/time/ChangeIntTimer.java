package dev.kabin.util.time;

public class ChangeIntTimer {

    private int curr;
    private int last;
    private long timeCurrent;

    public int curr() {
        return curr;
    }

    public void set(int value) {
        this.timeCurrent = 0L;
        this.last = curr;
        this.curr = value;
    }

    public long getTimeOnCurrent() {
        return timeCurrent;
    }

    public int last() {
        return last;
    }
}
