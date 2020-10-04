package dev.kabin.utilities.eventhandlers;

import dev.kabin.geometry.points.Point;
import dev.kabin.geometry.points.PointFloat;
import dev.kabin.global.GlobalData;
import dev.kabin.utilities.Functions;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MouseEventUtil implements EnumWithBoolHandler<MouseEventUtil.MouseButton> {

	private static final List<EventListener> changeListeners = new ArrayList<>();
	private static final Map<MouseButton, Boolean> currentMouseStates = new EnumMap<>(MouseButton.class);
	private static final Map<MouseButton, List<EventListener>> listenersPressed = new EnumMap<>(MouseButton.class);
	private static final Map<MouseButton, List<EventListener>> listenersReleased = new EnumMap<>(MouseButton.class);
	private static final Map<MouseButton, List<MouseDraggedEvent>> listenersMouseDrag = new EnumMap<>(MouseButton.class);
	private static final List<MouseScrollEvent> mouseScrollEvents = new ArrayList<>();
	private static final List<EventListener> defaultListeners = new ArrayList<>();
	private static final Map<MouseButton, PointFloat> dragStart = new EnumMap<>(MouseButton.class);
	private static MouseEventUtil instance;
	private static float x, y;

	protected MouseEventUtil() {
		// Register last time the position that the mouse began dragging.
		for (MouseButton b : MouseButton.values()) {
			addListener(b, true, () -> dragStart.put(b, Point.of(x, y)));
			addListener(b, false, () -> dragStart.remove(b));
		}
	}

	@NotNull
	public static MouseEventUtil getInstance() {
		return (instance != null) ? instance : (instance = new MouseEventUtil());
	}

	public static float getMouseX() {
		return (float) (x / GlobalData.getScale().x());
	}

	public static float getMouseY() {
		return (float) (y / GlobalData.getScale().y());
	}

	public static Optional<PointFloat> getDragStart(MouseButton b) {
		return Optional.ofNullable(dragStart.get(b));
	}

	public void registerMouseMoved(float x, float y) {
		EventUtil.setLastActive(EventUtil.LastActive.MOUSE);
		MouseEventUtil.x = x;
		MouseEventUtil.y = Functions.transformY(y, GlobalData.screenHeight);
		logger.info(() -> "\n" + "BLC: " + MouseEventUtil.x + ", " + MouseEventUtil.y + "\n"
				+ "TLC: " + x + "," + y);
	}

	@NotNull
	@Override
	public Map<MouseButton, Boolean> getCurrentStates() {
		return currentMouseStates;
	}

	@NotNull
	@Override
	public Map<MouseButton, List<EventListener>> getListenersPressed() {
		return listenersPressed;
	}

	@NotNull
	@Override
	public Map<MouseButton, List<EventListener>> getListenersReleased() {
		return listenersReleased;
	}

	@Override
	public @NotNull List<EventListener> getChangeListeners() {
		return changeListeners;
	}

	@NotNull
	@Override
	public List<EventListener> getDefaultListeners() {
		return defaultListeners;
	}

	public void registerMouseScroll(double scrollDelta) {
		//noinspection ForLoopReplaceableByForEach
		for (int i = 0, n = mouseScrollEvents.size(); i < n; i++) {
			mouseScrollEvents.get(i).onEvent(scrollDelta);
		}
	}

	public void registerMouseDragged(MouseButton button, float x, float y) {
		registerMouseMoved(x, y);
		if (listenersMouseDrag.containsKey(button)) {
			final List<MouseDraggedEvent> list = listenersMouseDrag.get(button);
			//noinspection ForLoopReplaceableByForEach
			for (int i = 0, n = list.size(); i < n; i++) {
				list.get(i).onDrag(x, y);
			}
		}
	}

	public void addMouseScrollListener(MouseScrollEvent event) {
		mouseScrollEvents.add(event);
	}

	public void addMouseDragListener(MouseButton mouseButton, MouseDraggedEvent event) {
		if (!listenersMouseDrag.containsKey(mouseButton)) {
			listenersMouseDrag.put(mouseButton, new ArrayList<>());
		}
		listenersMouseDrag.get(mouseButton).add(event);
	}

	@Override
	public void clear() {
		EnumWithBoolHandler.super.clear();
		mouseScrollEvents.clear();
		listenersMouseDrag.clear();
	}

	public enum MouseButton {
		RIGHT,
		LEFT,
		SCROLL
	}

	@FunctionalInterface
	public interface MouseScrollEvent {
		void onEvent(double deltaY);
	}

	@FunctionalInterface
	public interface MouseDraggedEvent {
		void onDrag(double x, double y);
	}

}