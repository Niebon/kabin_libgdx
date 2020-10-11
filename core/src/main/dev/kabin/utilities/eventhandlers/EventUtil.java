package dev.kabin.utilities.eventhandlers;


import dev.kabin.entities.Player;
import dev.kabin.global.GlobalData;
import dev.kabin.ui.DeveloperUI;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import static dev.kabin.global.GlobalData.developerMode;


public class EventUtil {

    private static InputOptions currentInputOptions;
    private static LastActive lastActive = LastActive.MOUSE;

    public static InputOptions getCurrentInputOptions() {
        return currentInputOptions;
    }

    public static void setInputOptions(@NotNull EventUtil.InputOptions options) {
        Player.getInstance().ifPresent(Player::freeze);
        currentInputOptions = options;
        final KeyEventUtil keyEventUtil = KeyEventUtil.getInstance();
        final MouseEventUtil mouseEventUtil = MouseEventUtil.getInstance();


        keyEventUtil.clear();
        mouseEventUtil.clear();


        // Menu trigger
        if (options.isHandleMenuEvents()) {
            keyEventUtil.addListener(KeyEventUtil.KeyCode.ESCAPE, true, () -> {/* TODO: implement*/});
        }

        // Listeners for player
        if (options.isHandlePlayerEvents()) {

            // Keyboard & mouse
            keyEventUtil.addListener(KeyEventUtil.KeyCode.F, true, () -> Player.getInstance().ifPresent(Player::triggerFlashLight));
            keyEventUtil.addListener(KeyEventUtil.KeyCode.SHIFT_LEFT, false, () -> Player.getInstance().ifPresent(Player::toggleRunSpeed));
            keyEventUtil.addListener(KeyEventUtil.KeyCode.SHIFT_LEFT, true, () -> Player.getInstance().ifPresent(Player::toggleWalkSpeed));
            keyEventUtil.addListener(KeyEventUtil.KeyCode.E, true, () -> Player.getInstance().ifPresent(Player::interactWithNearestIntractable));

            keyEventUtil.addChangeListener(EventListener.empty());

            mouseEventUtil.addListener(MouseEventUtil.MouseButton.RIGHT, true,
                    () -> Player.getInstance().ifPresent(p -> p.getHeldEntity().ifPresent(e -> p.releaseHeldEntity())));
            mouseEventUtil.addListener(MouseEventUtil.MouseButton.LEFT, true,
                    () -> Player.getInstance().ifPresent(p -> p.getHeldEntity().ifPresent(e -> p.throwHeldEntity()))
            );

        }
        // Listeners for toggle developer mode
        if (options.isHandleDevModeToggleEvent()) {
            keyEventUtil.addListener(KeyEventUtil.KeyCode.F12, true, () -> {
                developerMode = !developerMode;
                // TODO
                DeveloperUI.setVisible(GlobalData.developerMode);
                //UserInterface.showUserInterface(!GlobalData.developerMode);
            });
        }

        // Listeners toggle developer mode
        if (options.isHandleDevModeEvents()) {

            // Mouse events
            mouseEventUtil.addListener(MouseEventUtil.MouseButton.LEFT, true, () -> {
                if (KeyEventUtil.isShiftDown()) {
                    DeveloperUI.getEntityLoadingWidget().addEntity();
                }
                if (KeyEventUtil.isAltDown()) {
                    DeveloperUI.getTileSelectionWidget().addGroundTile();
                }
            });

            mouseEventUtil.addMouseDragListener(MouseEventUtil.MouseButton.RIGHT, (x, y) -> {
                if (KeyEventUtil.isAltDown()) DeveloperUI.TileSelectionWidget.addGroundTile();
            });

            mouseEventUtil.addMouseDragListener(MouseEventUtil.MouseButton.LEFT, (x, y) -> {
                if (KeyEventUtil.isAltDown()) {
                    DeveloperUI.TileSelectionWidget.removeGroundTileAtCurrentMousePosition();
                }
            });

            mouseEventUtil.addListener(MouseEventUtil.MouseButton.LEFT, true, () -> {
                if (KeyEventUtil.isControlDown()) {
                    DeveloperUI.addDevCue();
                }
            });


            // Keyboard events
            keyEventUtil.addListener(KeyEventUtil.KeyCode.S, true, () -> {
                if (KeyEventUtil.isControlDown() && developerMode) {
                    DeveloperUI.saveWorld();
                }
            });
            keyEventUtil.addListener(KeyEventUtil.KeyCode.Z, true, () -> {
                if (KeyEventUtil.isControlDown() && developerMode) {
                    DeveloperUI.undoChange();
                }
            });
            keyEventUtil.addListener(KeyEventUtil.KeyCode.Y, true, () -> {
                if (KeyEventUtil.isControlDown() && developerMode) {
                    DeveloperUI.redoChange();
                }
            });
        }
    }

    public static LastActive getLastActive() {
        return lastActive;
    }

    public static void setLastActive(LastActive lastActive) {
        EventUtil.lastActive = lastActive;
    }

    public enum LastActive {MOUSE, CONTROLLER}

    public static class InputOptions {

        private boolean handlePlayerEvents;
        private boolean handleDevModeEvents;
        private boolean handleDevModeToggleEvent;
        private boolean handleMenuEvents;
        private boolean handleControllerEvents;

        @NotNull
        @Contract(" -> new")
        public static InputOptions getRegisterAll() {
            return new InputOptions() {
                {
                    setHandleDevModeToggleEvent(true);
                    setHandleDevModeEvents(true);
                    setHandleMenuEvents(true);
                    setHandlePlayerEvents(true);
                    setHandleControllerEvents(true);
                }
            };
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

        public void setHandleMenuEvents(boolean handleMenuEvents) {
            this.handleMenuEvents = handleMenuEvents;
        }

        public boolean isHandleDevModeToggleEvent() {
            return handleDevModeToggleEvent;
        }

        public void setHandleDevModeToggleEvent(boolean handleDevModeToggleEvent) {
            this.handleDevModeToggleEvent = handleDevModeToggleEvent;
        }

        public boolean isHandleControllerEvents() {
            return handleControllerEvents;
        }

        public void setHandleControllerEvents(boolean handleControllerEvents) {
            this.handleControllerEvents = handleControllerEvents;
        }
    }
}
