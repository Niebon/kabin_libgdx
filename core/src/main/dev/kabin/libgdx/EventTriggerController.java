package dev.kabin.libgdx;


import dev.kabin.components.WorldRepresentation;
import dev.kabin.entities.Entity;
import dev.kabin.entities.libgdximpl.EntityGroup;
import dev.kabin.entities.libgdximpl.EntityLibgdx;
import dev.kabin.entities.libgdximpl.Player;
import dev.kabin.ui.developer.DeveloperUI;
import dev.kabin.util.events.KeyCode;
import dev.kabin.util.events.KeyEventUtil;
import dev.kabin.util.events.MouseEventUtil;
import dev.kabin.util.lambdas.FloatSupplier;
import dev.kabin.util.shapes.primitive.MutableRectInt;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class EventTriggerController {

    private LastActive lastActive = LastActive.MOUSE;
    private final KeyEventUtil keyEventUtil;
    private final MouseEventUtil mouseEventUtil;
    private final Supplier<WorldRepresentation<EntityGroup, EntityLibgdx>> representation;
    private Supplier<DeveloperUI> developerUISupplier;
    private final FloatSupplier scaleX;
    private final FloatSupplier scaleY;
    private boolean developerMode = false;

    public EventTriggerController(@NotNull EventTriggerController.InputOptions options,
                                  @NotNull KeyEventUtil keyEventUtil,
                                  @NotNull MouseEventUtil mouseEventUtil,
                                  @NotNull Supplier<WorldRepresentation<EntityGroup, EntityLibgdx>> representation,
                                  @NotNull Supplier<DeveloperUI> developerUISupplier,
                                  @NotNull FloatSupplier scaleX,
                                  @NotNull FloatSupplier scaleY) {
        this.keyEventUtil = keyEventUtil;
        this.mouseEventUtil = mouseEventUtil;
        this.representation = representation;
        this.developerUISupplier = developerUISupplier;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        setInputOptions(options);
    }

    public boolean isDeveloperMode() {
        return developerMode;
    }

    public void setDeveloperMode(boolean developerMode) {
        this.developerMode = developerMode;
    }

    public void setDeveloperUISupplier(Supplier<DeveloperUI> developerUISupplier) {
        this.developerUISupplier = developerUISupplier;
    }

    public void setInputOptions(@NotNull InputOptions options) {
        Player.getInstance().ifPresent(Player::freeze);

        keyEventUtil.clear();
        mouseEventUtil.clear();
        mouseEventUtil.addListener(() -> setLastActive(EventTriggerController.LastActive.MOUSE));

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

            keyEventUtil.addChangeListener(() -> {
            });

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
                developerUISupplier.get().setVisible(developerMode);
                Player.getInstance().ifPresent(player -> player.setHandleInput(!developerMode));
                //UserInterface.showUserInterface(!GlobalData.developerMode);
            });
        }

        // Listeners toggle developer mode
        if (options.isHandleDevModeEvents()) {

            // Mouse events
            {
                mouseEventUtil.addListener(MouseEventUtil.MouseButton.LEFT, true, () -> {
                    if (keyEventUtil.isShiftDown()) {
                        developerUISupplier.get().addEntity();
                    }
                });

                mouseEventUtil.addMouseDragListener(MouseEventUtil.MouseButton.LEFT, () -> {
                    if (keyEventUtil.isAltDown()) {
                        developerUISupplier.get().replaceCollisionTileAtCurrentMousePositionWithCurrentSelection();
                    }
                });

                mouseEventUtil.addMouseDragListener(MouseEventUtil.MouseButton.RIGHT, () -> {
                    if (keyEventUtil.isAltDown()) {
                        developerUISupplier.get().removeGroundTileAtCurrentMousePositionThreadLocked();
                    }
                });

                mouseEventUtil.addListener(MouseEventUtil.MouseButton.LEFT, true, () -> {
                    if (keyEventUtil.isControlDown()) {
                        developerUISupplier.get().addDevCue();
                    }
                });

                mouseEventUtil.addMouseScrollListener(val -> representation.get().getEntitiesWithinCameraBoundsCached(
                        MutableRectInt.centeredAt(
                                Math.round(mouseEventUtil.getMouseXRelativeToWorld() / scaleX.get()),
                                Math.round(mouseEventUtil.getMouseYRelativeToWorld() / scaleY.get()),
                                4,
                                4)
                ).stream().sorted(Entity::compareTo).findAny().ifPresent(e -> {
                    if (val > 0) {
                        e.setLayer(e.layer() + 1);
                        System.out.println("Modified layer: " + e.layer());
                    } else if (val < 0) {
                        e.setLayer(e.layer() - 1);
                        System.out.println("Modified layer: " + e.layer());
                    }
                }));
            }


            // Keyboard events
            {
                keyEventUtil.addListener(KeyCode.S, true, () -> {
                    if (keyEventUtil.isControlDown() && developerMode) {
                        developerUISupplier.get().saveWorld();
                    }
                });
                keyEventUtil.addListener(KeyCode.Z, true, () -> {
                    if (keyEventUtil.isControlDown() && developerMode) {
                        developerUISupplier.get().undoChange();
                    }
                });
                keyEventUtil.addListener(KeyCode.Y, true, () -> {
                    if (keyEventUtil.isControlDown() && developerMode) {
                        developerUISupplier.get().redoChange();
                    }
                });
            }
        }
    }

    public LastActive getLastActive() {
        return lastActive;
    }

    public void setLastActive(LastActive lastActive) {
        this.lastActive = lastActive;
    }

    public enum LastActive {MOUSE, CONTROLLER}

    public static final class InputOptions {

        private boolean handlePlayerEvents;
        private boolean handleDevModeEvents;
        private boolean handleDevModeToggleEvent;
        private boolean handleMenuEvents;
        private boolean handleControllerEvents;

        @NotNull
        @Contract(" -> new")
        public static InputOptions registerAll() {
            return new InputOptions()
                    .setHandleDevModeToggleEvent(true)
                    .setHandleDevModeEvents(true)
                    .setHandleMenuEvents(true)
                    .setHandlePlayerEvents(true)
                    .setHandleControllerEvents(true);
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

    }
}
