package dev.kabin.util.time;

public class SimpleCoolDown implements CoolDown {

    private long durationMillis;
    private long lastCheck = 0;
    private boolean frozen = true;

    public SimpleCoolDown(long durationMillis) {
        this.durationMillis = durationMillis;
    }

    @Override
    public void stop() {
        if (!frozen) {
            frozen = true;
            durationMillis = durationMillis - (System.currentTimeMillis() - lastCheck);
        }
    }

    @Override
    public void start() {
        if (frozen) {
            frozen = false;
            lastCheck = System.currentTimeMillis();
        }
    }

    @Override
    public boolean isActive() {
        return !frozen;
    }

    @Override
    public boolean isCompleted() {
        return !frozen && System.currentTimeMillis() - lastCheck > durationMillis;
    }


}
