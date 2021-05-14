package dev.kabin.util.time;

public class SimpleCooldown implements Cooldown {

    private long durationMillis;
    private long beganAtMillis = 0;
    private boolean frozen = true;

    public SimpleCooldown(long durationMillis) {
        this.durationMillis = durationMillis;
    }

    @Override
    public void stop() {
        if (!frozen && !isCompleted()) {
            frozen = true;
            durationMillis = durationMillis - (System.currentTimeMillis() - beganAtMillis);
        }
    }

    @Override
    public void start() {
        if (frozen || isCompleted()) {
            frozen = false;
            beganAtMillis = System.currentTimeMillis();
        }
    }

    @Override
    public boolean isActive() {
        return !frozen;
    }

    @Override
    public void forceComplete() {
        beganAtMillis = 0L;
    }

    @Override
    public boolean isCompleted() {
        return !frozen && System.currentTimeMillis() - beganAtMillis > durationMillis;
    }


}
