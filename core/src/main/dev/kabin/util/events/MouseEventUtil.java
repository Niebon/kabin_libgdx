package dev.kabin.util.events;

import com.badlogic.gdx.Gdx;
import dev.kabin.components.WorldRepresentation;
import dev.kabin.util.Functions;
import dev.kabin.util.lambdas.FloatSupplier;
import dev.kabin.util.points.PointFloat;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class MouseEventUtil implements EnumWithBoolHandler<MouseEventUtil.MouseButton> {

    private static final Logger logger = Logger.getLogger(EnumWithBoolHandler.class.getName());

    private final List<Runnable> changeListeners = new ArrayList<>();
    private final Map<MouseButton, Boolean> currentMouseStates = new EnumMap<>(MouseButton.class);
    private final Map<MouseButton, List<Runnable>> listenersPressed = new EnumMap<>(MouseButton.class);
    private final Map<MouseButton, List<Runnable>> listenersReleased = new EnumMap<>(MouseButton.class);
    private final Map<MouseButton, List<MouseDraggedEvent>> listenersMouseDrag = new EnumMap<>(MouseButton.class);
    private final List<MouseScrollEvent> mouseScrollEvents = new ArrayList<>();
    private final List<Runnable> defaultListeners = new ArrayList<>();
    private final Map<MouseButton, PointFloat> dragStart = new EnumMap<>(MouseButton.class);
    private final Supplier<WorldRepresentation<?, ?>> worldRepresentationSupplier;
    private final FloatSupplier camPosX;
    private final FloatSupplier camPosY;
    private float xRelativeToWorld, yRelativeToWorld;
    private float xRelativeToUI, yRelativeToUI;
    private final FloatSupplier scaleX, scaleY;

    public MouseEventUtil(Supplier<WorldRepresentation<?, ?>> worldRepresentationSupplier,
                          FloatSupplier camPosX,
                          FloatSupplier camPosY,
                          FloatSupplier scaleX, FloatSupplier scaleY) {
        initUnmodifiableListeners();
        this.worldRepresentationSupplier = worldRepresentationSupplier;
        this.camPosX = camPosX;
        this.camPosY = camPosY;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
    }

    public PointFloat getPositionRelativeToWorld() {
        return PointFloat.immutable(getMouseXRelativeToWorld(), getMouseYRelativeToWorld());
    }

    public PointFloat getPositionRelativeToUI() {
        return PointFloat.immutable(getXRelativeToUI(), getYRelativeToUI());
    }

    /**
     * @return The horizontal coordinate of the mouse position relative to the UI measured in
     * screen resolution coordinates. (If res = 1080p, then the output will be in the range [0, 1920)).
     */
    public float getXRelativeToUI() {
        return xRelativeToUI;
    }

    /**
     * @return The vertical coordinate of the mouse position relative to the UI measured in
     * screen resolution coordinates. (If res = 1080p, then the output will be in the range [0, 1080)).
     * If y = 0, then the position is at the bottom of the screen.
     */
    public float getYRelativeToUI() {
        return yRelativeToUI;
    }

    /**
     * @return The horizontal coordinate of the mouse in world coordinates. If the cameras bottom left corner is centered in the origin,
     * then the rectangle determined by the screen resolution (e.g. [0, 1920) × [0, 1080)) corresponds exactly to the rectangle [0, 400) × [0, 240) in world coordinates.
     * If the mouse stays perfectly in the mid-point of the screen, then this function returns 200.
     */
    public float getMouseXRelativeToWorld() {
        return xRelativeToWorld;
    }

    /**
     * @return The vertical coordinate of the mouse in world coordinates. If the cameras bottom left corner is centered in the origin,
     * then the rectangle determined by the screen resolution (e.g. [0, 1920) × [0, 1080)) corresponds exactly to the rectangle [0, 400) × [0, 240) in world coordinates.
     * If the mouse stays perfectly in the mid-point of the screen, then this function returns 120.
     */
    public float getMouseYRelativeToWorld() {
        return yRelativeToWorld;
    }

    public Optional<PointFloat> getDragStart(MouseButton b) {
        return Optional.ofNullable(dragStart.get(b));
    }

    private void initUnmodifiableListeners() {
        // Register last time the position that the mouse began dragging.
        for (MouseButton b : MouseButton.values()) {
            addListener(b, true, () -> dragStart.put(b, PointFloat.immutable(xRelativeToWorld, yRelativeToWorld)));
            addListener(b, false, () -> dragStart.remove(b));
        }

        addListener(MouseButton.LEFT, true, () -> logger.warning(this::infoCurrentHover));
    }

    private String infoCurrentHover() {
        final WorldRepresentation<?, ?> worldState = worldRepresentationSupplier.get();
        return "Info found about point: \n" +
                "mouseRelToWorld: " + getPositionRelativeToWorld() + "\n" +
                "mouseRelToWorldUnscaled: " + getPositionRelativeToWorld().scaleBy(1 / scaleX.get()).toPointInt() + "\n" +
                "mouseRelToUI: " + getPositionRelativeToUI() + "\n" +
                "mouseRelToUIUnscaled: " + getPositionRelativeToUI().scaleBy(1 / scaleY.get()).toPointInt() + "\n" +
                "collision: " + (worldState != null ? worldState.getCollision(
                Math.round(getMouseXRelativeToWorld()),
                Math.round(getMouseYRelativeToWorld())
        ) : "");
    }

    // TODO: Fix.
    public void registerMouseMoved(float x, float y) {
        xRelativeToUI = x;
        yRelativeToUI = Functions.transformY(y, Gdx.graphics.getHeight());

        float offsetX = camPosX.get() - Gdx.graphics.getWidth() * 0.5f;
        float offsetY = camPosY.get() - Gdx.graphics.getHeight() * 0.5f;

        xRelativeToWorld = (x + offsetX) / scaleX.get();
        yRelativeToWorld = (Functions.transformY(y, Gdx.graphics.getHeight()) + offsetY) / scaleY.get();
        logger.info(() -> "\n" + "BLC: " + xRelativeToWorld + ", " + yRelativeToWorld + "\n"
                + "TLC: " + x + "," + y);
    }

    @NotNull
    @Override
    public Map<MouseButton, Boolean> getCurrentStates() {
        return currentMouseStates;
    }

    @NotNull
    @Override
    public Map<MouseButton, List<Runnable>> getListenersPressed() {
        return listenersPressed;
    }

    @NotNull
    @Override
    public Map<MouseButton, List<Runnable>> getListenersReleased() {
        return listenersReleased;
    }

    @Override
    public @NotNull List<Runnable> getChangeListeners() {
        return changeListeners;
    }

    @NotNull
    @Override
    public List<Runnable> getDefaultListeners() {
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