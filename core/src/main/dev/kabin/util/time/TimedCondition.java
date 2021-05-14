package dev.kabin.util.time;

public class TimedCondition {

	private final boolean stateWhileActive;
	private final Cooldown cooldown;
	private boolean initialized = false;

	/**
	 * A boolean that has the given initial value for a set period of time.
	 * Afterwards, it changes to the negation.
	 *
	 * @param stateWhileActive    the state while the condition is active (either {@code true} or {@code false}).
	 * @param inversionTimeMillis the duration before the condition inverts.
	 */
	public TimedCondition(boolean stateWhileActive, long inversionTimeMillis) {
		this.stateWhileActive = stateWhileActive;
		cooldown = new SimpleCooldown(inversionTimeMillis);
	}

	public void init() {
		if (!initialized) {
			initialized = true;
			cooldown.start();
		}
	}

	public void reset() {
		cooldown.reset();
		initialized = false;
	}

	public boolean eval() {
		return initialized && !cooldown.isCompleted() && stateWhileActive;
	}
}
