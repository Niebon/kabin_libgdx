package dev.kabin.utilities.eventhandlers;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class KeyEventUtil implements EnumWithBoolHandler<KeyEventUtil.KeyCode>{

    public enum KeyCode{
        Q,W,E,R,T,Y,U,I,O,P,A,S,D,F,G,H,J,K,L,Z,X,C,V,B,N,M,
        CONTROL_LEFT, SHIFT_LEFT, ALT_LEFT
    }

    private static KeyEventUtil instance;

    private static final List<EventListener> changeListeners = new ArrayList<>();
    private static final Map<KeyCode, Boolean> currentKeyStates = new EnumMap<>(KeyCode.class);
    private static final Map<KeyCode, List<EventListener>> listenersPressed = new EnumMap<>(KeyCode.class);
    private static final Map<KeyCode, List<EventListener>> listenersReleased = new EnumMap<>(KeyCode.class);
    private static final List<EventListener> defaultListeners = new ArrayList<>();

    protected KeyEventUtil(){}

    @NotNull
    public static KeyEventUtil getInstance() {
        return (instance != null) ? instance : (instance = new KeyEventUtil());
    }

    @NotNull
    @Override
    public Map<KeyCode, Boolean> getCurrentStates() {
        return currentKeyStates;
    }

    @NotNull
    @Override
    public Map<KeyCode, List<EventListener>> getListenersPressed() {
        return listenersPressed;
    }

    @NotNull
    @Override
    public Map<KeyCode, List<EventListener>> getListenersReleased() {
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

    public static boolean isControlDown(){
        return currentKeyStates.containsKey(KeyCode.CONTROL_LEFT) && currentKeyStates.get(KeyCode.CONTROL_LEFT);
    }

    public static boolean isShiftDown(){
        return currentKeyStates.containsKey(KeyCode.SHIFT_LEFT) && currentKeyStates.get(KeyCode.SHIFT_LEFT);
    }

    public static boolean isAltDown() {
        return currentKeyStates.containsKey(KeyCode.ALT_LEFT) && currentKeyStates.get(KeyCode.ALT_LEFT);
    }

}