package dev.kabin.util;

public interface CoolDown {

    static Builder builder() {
        return new Builder();
    }

    void stop();

    void start();

    boolean isCompleted();

    boolean isActive();

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

        public CoolDown build() {
            return waitBeforeAcceptStart == 0f ? new SimpleCoolDown(durationMillis) : DelayedCooldown.of(durationMillis, waitBeforeAcceptStart);
        }

    }

}
