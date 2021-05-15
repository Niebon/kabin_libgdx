package dev.kabin.util.eventhandlers;

import java.util.ArrayList;
import java.util.HashMap;

public class IntChangeListener {

	private final HashMap<Integer, ArrayList<Runnable>> actions = new HashMap<>();
	private int curr;
	private int last;

	public int curr() {
		return curr;
	}

	public void set(int value) {
		if (value != curr) {
			this.last = curr;
			this.curr = value;
			if (actions.containsKey(value)) {
				actions.get(value).forEach(Runnable::run);
			}
		}
	}

	public int last() {
		return last;
	}

	public void addListener(int value, Runnable action) {
		if (!actions.containsKey(value)) {
			actions.put(value, new ArrayList<>());
		}
		actions.get(value).add(action);
	}

	public void clear() {
		actions.clear();
	}

}
