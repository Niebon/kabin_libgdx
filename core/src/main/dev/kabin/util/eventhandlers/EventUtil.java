package dev.kabin.util.eventhandlers;


import dev.kabin.GlobalData;
import dev.kabin.components.WorldRepresentation;
import dev.kabin.entities.impl.Entity;
import dev.kabin.entities.impl.Player;
import dev.kabin.ui.developer.DeveloperUI;
import dev.kabin.util.shapes.primitive.MutableRectInt;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

import static dev.kabin.GlobalData.developerMode;


public class EventUtil {

    private static InputOptions currentInputOptions;
    private static LastActive lastActive = LastActive.MOUSE;

    public static InputOptions getCurrentInputOptions() {
        return currentInputOptions;
    }

    public static void setInputOptions(@NotNull EventUtil.InputOptions options,
                                       KeyEventUtil keyEventUtil,
                                       MouseEventUtil mouseEventUtil,
                                       Supplier<WorldRepresentation> representation) {
        Player.getInstance().ifPresent(Player::freeze);
        currentInputOptions = options;


        keyEventUtil.clear();
        mouseEventUtil.clear();


        // Menu trigger
        if (options.isHandleMenuEvents()) {
            keyEventUtil.addListener(KeyCode.ESCAPE, true, () -> {/* TODO: implement*/});
        }

        // Listeners for player
        if (options.isHandlePlayerEvents()) {

            // Keyboard & mouse
            keyEventUtil.addListener(KeyCode.F, true, () -> Player.getInstance().ifPresent(Player::triggerFlashLight));
            keyEventUtil.addListener(KeyCode.SHIFT_LEFT, false, () -> Player.getInstance().ifPresent(Player::toggleRunSpeed));
            keyEventUtil.addListener(KeyCode.SHIFT_LEFT, true, () -> Player.getInstance().ifPresent(Player::toggleWalkSpeed));
            keyEventUtil.addListener(KeyCode.E, true, () -> Player.getInstance().ifPresent(Player::interactWithNearestIntractable));

            keyEventUtil.addChangeListener(EventListener.empty());

            mouseEventUtil.addListener(MouseEventUtil.MouseButton.RIGHT, true,
                    () -> Player.getInstance().ifPresent(p -> p.getHeldEntity().ifPresent(e -> p.releaseHeldEntity())));
            mouseEventUtil.addListener(MouseEventUtil.MouseButton.LEFT, true,
                    () -> Player.getInstance().ifPresent(p -> p.getHeldEntity().ifPresent(e -> p.throwHeldEntity()))
            );

        }
        // Listeners for toggle developer mode
        if (options.isHandleDevModeToggleEvent()) {
            keyEventUtil.addListener(KeyCode.F12, true, () -> {
                developerMode = !developerMode;
                // TODO
                DeveloperUI.setVisible(GlobalData.developerMode);

                //UserInterface.showUserInterface(!GlobalData.developerMode);
            });
        }

        // Listeners toggle developer mode
        if (options.isHandleDevModeEvents()) {

            // Mouse events
            {
                mouseEventUtil.addListener(MouseEventUtil.MouseButton.LEFT, true, () -> {
                    if (keyEventUtil.isShiftDown()) {
                        DeveloperUI.getEntityLoadingWidget().addEntity();
                    }
                });

                mouseEventUtil.addMouseDragListener(MouseEventUtil.MouseButton.LEFT, () -> {
                    if (keyEventUtil.isAltDown()) {
                        DeveloperUI.getTileSelectionWidget().replaceCollisionTileAtCurrentMousePositionWithCurrentSelection();
                    }
                });

                mouseEventUtil.addMouseDragListener(MouseEventUtil.MouseButton.RIGHT, () -> {
                    if (keyEventUtil.isAltDown()) {
                        DeveloperUI.getTileSelectionWidget().removeGroundTileAtCurrentMousePositionThreadLocked();
                    }
                });

                mouseEventUtil.addListener(MouseEventUtil.MouseButton.LEFT, true, () -> {
                    if (keyEventUtil.isControlDown()) {
                        DeveloperUI.addDevCue();
                    }
                });

                mouseEventUtil.addMouseScrollListener(val -> {
                    representation.get().getEntitiesWithinCameraBoundsCached(
                            MutableRectInt.centeredAt(
                                    Math.round(mouseEventUtil.getMouseXRelativeToWorld() / options.getScale()),
                                    Math.round(mouseEventUtil.getMouseYRelativeToWorld() / options.getScale()),
                                    4,
                                    4)
                    ).stream().sorted(Entity::compareTo).findAny().ifPresent(e -> {
                        if (val > 0) {
                            e.setLayer(e.getLayer() + 1);
                            System.out.println("Modified layer: " + e.getLayer());
                        } else if (val < 0) {
                            e.setLayer(e.getLayer() - 1);
                            System.out.println("Modified layer: " + e.getLayer());
                        }
                    });
                });
            }


            // Keyboard events
            {
                keyEventUtil.addListener(KeyCode.S, true, () -> {
                    if (keyEventUtil.isControlDown() && developerMode) {
                        DeveloperUI.saveWorld();
                    }
                });
                keyEventUtil.addListener(KeyCode.Z, true, () -> {
                    if (keyEventUtil.isControlDown() && developerMode) {
                        DeveloperUI.undoChange();
                    }
                });
                keyEventUtil.addListener(KeyCode.Y, true, () -> {
                    if (keyEventUtil.isControlDown() && developerMode) {
                        DeveloperUI.redoChange();
                    }
                });
            }
        }
    }

    public static LastActive getLastActive() {
        return lastActive;
    }

    public static void setLastActive(LastActive lastActive) {
        EventUtil.lastActive = lastActive;
    }

    public enum LastActive {MOUSE, CONTROLLER}

    public static final class InputOptions {

        private boolean handlePlayerEvents;
        private boolean handleDevModeEvents;
        private boolean handleDevModeToggleEvent;
        private boolean handleMenuEvents;
        private boolean handleControllerEvents;
        private float scale = 1;

        @NotNull
        @Contract("_-> new")
        public static InputOptions registerAll(float scale) {
            return new InputOptions()
                    .setHandleDevModeToggleEvent(true)
                    .setHandleDevModeEvents(true)
                    .setHandleMenuEvents(true)
                    .setHandlePlayerEvents(true)
                    .setHandleControllerEvents(true)
                    .setScale(scale);
        }

        public boolean isHandlePlayerEvents() {
            return handlePlayerEvents;
        }

        public InputOptions setHandlePlayerEvents(boolean handlePlayerEvents) {
            this.handlePlayerEvents = handlePlayerEvents;
            return this;
        }

        public boolean isHandleDevModeEvents() {
            return handleDevModeEvents;
        }

        public InputOptions setHandleDevModeEvents(boolean handleDevModeEvents) {
            this.handleDevModeEvents = handleDevModeEvents;
            return this;
        }

        public boolean isHandleMenuEvents() {
            return handleMenuEvents;
        }

        public InputOptions setHandleMenuEvents(boolean handleMenuEvents) {
            this.handleMenuEvents = handleMenuEvents;
            return this;
        }

        public boolean isHandleDevModeToggleEvent() {
            return handleDevModeToggleEvent;
        }

        public InputOptions setHandleDevModeToggleEvent(boolean handleDevModeToggleEvent) {
            this.handleDevModeToggleEvent = handleDevModeToggleEvent;
            return this;
        }

        public boolean isHandleControllerEvents() {
            return handleControllerEvents;
        }

        public InputOptions setHandleControllerEvents(boolean handleControllerEvents) {
            this.handleControllerEvents = handleControllerEvents;
            return this;
        }

        float getScale() {
            return scale;
        }

        public InputOptions setScale(float scale) {
            this.scale = scale;
            return this;
        }
    }
}
