package dev.kabin.util.time;

public class TimedCondition {

	private final boolean stateWhileActive;
	private final Cooldown cooldown;

	/**
	 * A boolean that has the given initial value for a set period of time.
	 * Afterwards, it changes to the negation.
	 *
	 * @param stateWhileActive  the state while the condition is active (either {@code true} or {@code false}).
	 * @param invertAfterMillis the duration before the condition inverts.
	 */
	public TimedCondition(boolean stateWhileActive, long invertAfterMillis) {
		this.stateWhileActive = stateWhileActive;
		cooldown = Cooldown.builder().setDurationMillis(invertAfterMillis).build();
	}

	public void init() {
		cooldown.init();
	}

	public boolean eval() {
		return cooldown.isActive() && !cooldown.isCompleted() && stateWhileActive;
	}
}
