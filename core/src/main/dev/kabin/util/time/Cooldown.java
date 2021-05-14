package dev.kabin.util.time;

public interface Cooldown {

    static Builder builder() {
        return new Builder();
    }

    void reset();

    void start();

    void pause();

    void unpause();

    boolean isCompleted();

    boolean isActive();

    void forceComplete();

    class Builder {

        long durationMillis;
        long waitBeforeAcceptStart;

        public Builder setDurationMillis(long durationMillis) {
            this.durationMillis = durationMillis;
            return this;
        }

        public Builder setWaitBeforeAcceptStart(long waitBeforeAcceptStart) {
            this.waitBeforeAcceptStart = waitBeforeAcceptStart;
            return this;
        }

        public Cooldown build() {
            return waitBeforeAcceptStart == 0f
                    ? new SimpleCooldown(durationMillis)
                    : DelayedCooldown.of(durationMillis, waitBeforeAcceptStart);
        }

    }

}
