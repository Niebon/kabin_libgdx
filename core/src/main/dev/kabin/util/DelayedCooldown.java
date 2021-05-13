package dev.kabin.util;

public class DelayedCooldown implements CoolDown {

    private final CoolDown first;
    private final CoolDown second;

    private DelayedCooldown(CoolDown first, CoolDown second) {
        this.first = first;
        this.second = second;
    }

    public static DelayedCooldown of(long durationMillis, long waitBeforeAcceptStart) {
        var first = new SimpleCoolDown(waitBeforeAcceptStart);
        var dc = new DelayedCooldown(first, new SimpleCoolDown(durationMillis));
        first.start();
        return dc;
    }

    @Override
    public void stop() {
        if (first.isCompleted()) {
            second.stop();
        }
    }

    @Override
    public void start() {
        if (first.isCompleted()) {
            second.start();
        }
    }

    @Override
    public boolean isCompleted() {
        return first.isCompleted() && second.isCompleted();
    }

    @Override
    public boolean isActive() {
        return first.isCompleted() && second.isActive();
    }
}
