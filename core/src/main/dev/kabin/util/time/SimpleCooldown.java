package dev.kabin.util.time;

public class SimpleCooldown implements Cooldown {

    private final long durationMillis;
    private long beganAtMillis = 0L;
    private long accumulatedPauseDurationMillis = 0L;
    private long lastPauseStart = 0L;
    private boolean paused = true;
    private boolean mayStart = true;

    public SimpleCooldown(long durationMillis) {
        this.durationMillis = durationMillis;
    }

    @Override
    public void reset() {
        if (isCompleted()) {
            mayStart = true;
        }
    }

    @Override
    public void start() {
        if (mayStart && System.currentTimeMillis() - (beganAtMillis + accumulatedPauseDurationMillis) > durationMillis) {
            beganAtMillis = System.currentTimeMillis();
            paused = false;
            mayStart = false;
        }
    }

    @Override
    public void pause() {
        if (!paused && !isCompleted()) {
            paused = true;
            lastPauseStart = System.currentTimeMillis();
        }
    }

    @Override
    public void unpause() {
        if (paused || isCompleted()) {
            paused = false;
            accumulatedPauseDurationMillis += System.currentTimeMillis() - lastPauseStart;
        }
    }

    @Override
    public boolean isActive() {
        return !paused && !mayStart;
    }

    @Override
    public void forceComplete() {
        beganAtMillis = 0L;
        paused = false;
    }

    @Override
    public boolean isCompleted() {
        return paused ?
                //System.currentTimeMillis() - (accumulatedPauseDurationMillis + (System.currentTimeMillis() - lastPauseStart)) - beganAtMillis > durationMillis
                lastPauseStart - accumulatedPauseDurationMillis - beganAtMillis > durationMillis // Above expression "simplified".
                : System.currentTimeMillis() - (beganAtMillis + accumulatedPauseDurationMillis) > durationMillis;
    }


}
