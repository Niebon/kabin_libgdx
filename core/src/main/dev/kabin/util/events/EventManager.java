package dev.kabin.util.events;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public record EventManager(Map<String, List<Runnable>> eventListener) {

	public void addListener(String event, Runnable action) {
		if (!eventListener.containsKey(event)) {
			eventListener.put(event, new ArrayList<>());
		}
		eventListener.get(event).add(action);
	}

	public void trigger(String event) {
		if (eventListener.containsKey(event)) {
			eventListener.get(event).forEach(Runnable::run);
		}
	}

}
