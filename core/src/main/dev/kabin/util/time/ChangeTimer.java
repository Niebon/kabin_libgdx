package dev.kabin.util.time;

public class ChangeTimer<T> {

    private T value;
    private long timeCurrent;

    public T get() {
        return value;
    }

    public void set(T value) {
        this.timeCurrent = 0L;
        this.value = value;
    }

    public long getTimeCurrent() {
        return timeCurrent;
    }

}
