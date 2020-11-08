package dev.kabin.components;

import org.jetbrains.annotations.Contract;

import java.util.HashMap;


public class ComponentParameters {

    private int x, y;
    private int width, height;
    private float scaleFactor;
    private boolean hasSubcomponents = true;

    public static final int COARSENESS_PARAMETER = 256;

    @Contract("_ -> this")
    ComponentParameters setX(int x) {
        if (hasSubcomponents(x)) this.x = x;
        else hasSubcomponents = false;
        return this;
    }

    @Contract("_ -> this")
    ComponentParameters setY(int y) {
        if (hasSubcomponents(y)) this.y = y;
        else hasSubcomponents = false;
        return this;
    }

    @Contract("_ -> this")
    ComponentParameters setHeight(int height) {
        if (hasSubcomponents(height)) this.height = height;
        else hasSubcomponents = false;
        return this;
    }

    @Contract("_ -> this")
    ComponentParameters setWidth(int width) {
        if (hasSubcomponents(width)) this.width = width;
        else hasSubcomponents = false;
        return this;
    }

    @Contract("_ -> this")
    ComponentParameters setScaleFactor(float scaleFactor) {
        this.scaleFactor = scaleFactor;
        return this;
    }

    /**
     * @return false iff the parameters are invalid.
     */
    public boolean hasSubcomponents() {return hasSubcomponents;}

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public float getScaleFactor() {
        return scaleFactor;
    }

    private static boolean hasSubcomponents(int value) {
        return Math.floorMod(value, COARSENESS_PARAMETER) == 0;
    }

    @Override
    public String toString() {
        return new HashMap<>(){
            {
                put("x", getX());
                put("y", getY());
                put("width", getWidth());
                put("height", getHeight());
                put("scaleFactor", getScaleFactor());
                put("hasSubcomponents", hasSubcomponents());

            }
        }.toString();

    }
}
