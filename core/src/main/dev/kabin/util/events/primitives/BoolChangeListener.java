package dev.kabin.util.events.primitives;

import java.util.ArrayList;
import java.util.HashMap;

public class BoolChangeListener {

	private final HashMap<Boolean, ArrayList<Runnable>> actions = new HashMap<>();
	private boolean curr;
	private boolean last;
	private boolean lastSetValue;

	public boolean get() {
		return curr;
	}

	public void set(boolean value) {
		lastSetValue = value;
		if (value != curr) {
			this.last = curr;
			this.curr = value;
			if (actions.containsKey(value)) {
				actions.get(value).forEach(Runnable::run);
			}
		}
	}

	public boolean last() {
		return last;
	}

	public boolean isLastSetValue() {
		return lastSetValue;
	}

	public void addListener(boolean value, Runnable action) {
		if (!actions.containsKey(value)) {
			actions.put(value, new ArrayList<>());
		}
		actions.get(value).add(action);
	}

	public void clear() {
		actions.clear();
	}

}
