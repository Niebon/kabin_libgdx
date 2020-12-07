package dev.kabin.utilities.eventhandlers;

import dev.kabin.GlobalData;
import dev.kabin.utilities.Functions;
import dev.kabin.utilities.points.Point;
import dev.kabin.utilities.points.PointFloat;
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
    private static float xRelativeToWorld, yRelativeToWorld;
    private static float xRelativeToUI, yRelativeToUI;

    /**
     * The mouse event utility has a state which is overridden each time a call to {@link #clear()} is made.
     * This includes any listeners adding during the constructor.
     */
    protected MouseEventUtil() {
        // Hence we make a call to unmodifiable listeners.
        initUnmodifiableListeners();
    }

    @NotNull
    public static MouseEventUtil getInstance() {
        return (instance != null) ? instance : (instance = new MouseEventUtil());
    }

    public static PointFloat getPositionRelativeToWorld() {
        return Point.of(getMouseXRelativeToWorld(), getMouseYRelativeToWorld());
    }

    public static PointFloat getPositionRelativeToUI() {
        return Point.of(getXRelativeToUI(), getYRelativeToUI());
    }

    public static float getXRelativeToUI() {
        return (float) (xRelativeToUI / GlobalData.getScale().x());
    }

    public static float getYRelativeToUI() {
        return (float) (yRelativeToUI / GlobalData.getScale().x());
    }

    public static float getMouseXRelativeToWorld() {
        return (float) (xRelativeToWorld / GlobalData.getScale().x());
    }

    public static float getMouseYRelativeToWorld() {
        return (float) (yRelativeToWorld / GlobalData.getScale().y());
    }

    public static Optional<PointFloat> getDragStart(MouseButton b) {
        return Optional.ofNullable(dragStart.get(b));
    }

    private void initUnmodifiableListeners() {
        // Register last time the position that the mouse began dragging.
        for (MouseButton b : MouseButton.values()) {
            addListener(b, true, () -> dragStart.put(b, Point.of(xRelativeToWorld, yRelativeToWorld)));
            addListener(b, false, () -> dragStart.remove(b));
        }

        addListener(MouseButton.LEFT, true, () -> LOGGER.warning(this::infoCurrentHover));
    }

    private String infoCurrentHover() {
        return "Info found about point: \n" +
                "mouseRelToWorld: " + getPositionRelativeToWorld() + "\n" +
                "mouseRelToWorldUnscaled: " + getPositionRelativeToWorld().scaleThis(1 / GlobalData.scaleFactor).toPointInt() + "\n" +
                "mouseRelToUI: " + getPositionRelativeToUI() + "\n" +
                "mouseRelToUIUnscaled: " + getPositionRelativeToUI().scaleThis(1 / GlobalData.scaleFactor).toPointInt() + "\n" +
                "collision: " + (GlobalData.getRootComponent() != null ? GlobalData.getRootComponent().getCollision(
                Math.round(getMouseXRelativeToWorld() / GlobalData.scaleFactor),
                Math.round(getMouseYRelativeToWorld() / GlobalData.scaleFactor)
        ) : "");
    }

    public void registerMouseMoved(float x, float y) {
        EventUtil.setLastActive(EventUtil.LastActive.MOUSE);
        MouseEventUtil.xRelativeToUI = x;
        MouseEventUtil.yRelativeToUI = Functions.transformY(y, GlobalData.screenHeight);

        // camera.x and camera.y are in the middle of the screen. Hence the offsets:
        float offsetX = GlobalData.camera.position.x - GlobalData.screenWidth * 0.5f;
        float offsetY = GlobalData.camera.position.y - GlobalData.screenHeight * 0.5f;

        MouseEventUtil.xRelativeToWorld = x + offsetX;
        MouseEventUtil.yRelativeToWorld = Functions.transformY(y, GlobalData.screenHeight) + offsetY;
        LOGGER.info(() -> "\n" + "BLC: " + MouseEventUtil.xRelativeToWorld + ", " + MouseEventUtil.yRelativeToWorld + "\n"
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

        // Transforms MouseUtil.x and MouseUtil.y to correct coord relative to world.
        registerMouseMoved(x, y);

        if (listenersMouseDrag.containsKey(button)) {
            final List<MouseDraggedEvent> list = listenersMouseDrag.get(button);
            //noinspection ForLoopReplaceableByForEach
            for (int i = 0, n = list.size(); i < n; i++) {
                list.get(i).onDrag();
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
        initUnmodifiableListeners();
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
        void onDrag();
    }

}