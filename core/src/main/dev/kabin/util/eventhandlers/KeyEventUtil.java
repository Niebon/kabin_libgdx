package dev.kabin.util.eventhandlers;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class KeyEventUtil implements EnumWithBoolHandler<KeyCode> {


    private final List<Runnable> changeListeners = new ArrayList<>();
    private final Map<KeyCode, Boolean> currentKeyStates = new EnumMap<>(KeyCode.class);
    private final Map<KeyCode, List<Runnable>> listenersPressed = new EnumMap<>(KeyCode.class);
    private final Map<KeyCode, List<Runnable>> listenersReleased = new EnumMap<>(KeyCode.class);
    private final List<Runnable> defaultListeners = new ArrayList<>();

    public KeyEventUtil() {
    }

    @NotNull
    @Override
    public Map<KeyCode, Boolean> getCurrentStates() {
        return currentKeyStates;
    }

    @NotNull
    @Override
    public Map<KeyCode, List<Runnable>> getListenersPressed() {
        return listenersPressed;
    }

    @NotNull
    @Override
    public Map<KeyCode, List<Runnable>> getListenersReleased() {
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

    public boolean isControlDown(){
        return currentKeyStates.containsKey(KeyCode.CONTROL_LEFT) && currentKeyStates.get(KeyCode.CONTROL_LEFT);
    }

    public boolean isShiftDown(){
        return currentKeyStates.containsKey(KeyCode.SHIFT_LEFT) && currentKeyStates.get(KeyCode.SHIFT_LEFT);
    }

    public boolean isAltDown() {
        return currentKeyStates.containsKey(KeyCode.ALT_LEFT) && currentKeyStates.get(KeyCode.ALT_LEFT);
    }

}