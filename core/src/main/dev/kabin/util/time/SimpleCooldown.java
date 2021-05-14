package dev.kabin.util.time;

public class SimpleCooldown implements Cooldown {

    private long durationMillis;
    private long beganAtMillis = 0;
    private boolean paused = true;

    public SimpleCooldown(long durationMillis) {
        this.durationMillis = durationMillis;
    }

    @Override
    public void init() {
        if (System.currentTimeMillis() - beganAtMillis > durationMillis) {
            beganAtMillis = System.currentTimeMillis();
            paused = false;
        }
    }

    @Override
    public void pause() {
        if (!paused && !isCompleted()) {
            paused = true;
            durationMillis = durationMillis - (System.currentTimeMillis() - beganAtMillis);
        }
    }

    @Override
    public void unpause() {
        if (paused || isCompleted()) {
            paused = false;
            beganAtMillis = System.currentTimeMillis();
        }
    }

    @Override
    public boolean isActive() {
        return !paused;
    }

    @Override
    public void forceComplete() {
        beganAtMillis = 0L;
        paused = false;
    }

    @Override
    public boolean isCompleted() {
        return !paused && System.currentTimeMillis() - beganAtMillis > durationMillis;
    }


}
