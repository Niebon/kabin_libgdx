package dev.kabin.util.events;

public final class IntChangeListenerTimed implements IntChangeListener {

    private final IntChangeListener intChangeListener;
    private long lastChange = System.currentTimeMillis();

    public IntChangeListenerTimed() {
        this.intChangeListener = new IntChangeListenerSimple();
    }

    public IntChangeListener intChangeListener() {
        return intChangeListener;
    }

    @Override
    public int get() {
        return intChangeListener.get();
    }

    @Override
    public boolean set(int value) {
        if (intChangeListener.set(value)) {
            lastChange = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    @Override
    public int last() {
        return intChangeListener.last();
    }

    @Override
    public void addListener(int value, Runnable action) {
        intChangeListener.addListener(value, action);
    }

    @Override
    public void clear() {
        intChangeListener.clear();
    }

    public long timeCurrentStateMillis() {
        return System.currentTimeMillis() - lastChange;
    }

}
