package dev.kabin.util.time;

@SuppressWarnings("ClassCanBeRecord") // Don't expose first & second.
public class DelayedCooldown implements Cooldown {

    private final Cooldown first;
    private final Cooldown second;

    private DelayedCooldown(Cooldown first, Cooldown second) {
        this.first = first;
        this.second = second;
    }

    public static DelayedCooldown of(long durationMillis, long waitBeforeAcceptStart) {
        var first = new SimpleCooldown(waitBeforeAcceptStart);
        var dc = new DelayedCooldown(first, new SimpleCooldown(durationMillis));
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

    @Override
    public void forceComplete() {
        first.forceComplete();
        second.forceComplete();
    }
}
