package dev.kabin;

public class Cooldown {

    private final long durationMillis;
    private long lastCheck = 0;

    public Cooldown(long durationMillis) {
        this.durationMillis = durationMillis;
    }

    public void trigger() {
        lastCheck = System.currentTimeMillis();
    }

    public boolean isReady() {
        return System.currentTimeMillis() - lastCheck > durationMillis;
    }
}
