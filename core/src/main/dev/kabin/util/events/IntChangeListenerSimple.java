package dev.kabin.util.events;

import java.util.ArrayList;
import java.util.HashMap;

public class IntChangeListenerSimple implements IntChangeListener {

	private final HashMap<Integer, ArrayList<Runnable>> actions = new HashMap<>();
	private int curr;
	private int last;

	@Override
	public int get() {
		return curr;
	}

	@Override
	public boolean set(int value) {
		if (value != curr) {
			this.last = curr;
			this.curr = value;
			if (actions.containsKey(value)) {
				actions.get(value).forEach(Runnable::run);
			}
			return true;
		}
		return false;
	}

	@Override
	public int last() {
		return last;
	}

	@Override
	public void addListener(int value, Runnable action) {
		if (!actions.containsKey(value)) {
			actions.put(value, new ArrayList<>());
		}
		actions.get(value).add(action);
	}

	@Override
	public void clear() {
		actions.clear();
	}

}
