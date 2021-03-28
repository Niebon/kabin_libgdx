package dev.kabin.components;

import org.jetbrains.annotations.Contract;

import java.util.HashMap;

public class ComponentParameters {

    public static final int COARSENESS_PARAMETER = 64; // 256;

    private final int x, y;
    private final int width, height;
    private final float scaleFactor;
    private final boolean hasSubcomponents;


    private ComponentParameters(int x,
                                int y,
                                int width,
                                int height,
                                float scaleFactor,
                                boolean hasSubcomponents) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.scaleFactor = scaleFactor;
        this.hasSubcomponents = hasSubcomponents;
    }

    public static Builder builder() {
        return new Builder();
    }


    /**
     * @return false iff the parameters are invalid.
     */
    public boolean hasSubcomponents() {
        return hasSubcomponents;
    }

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

    public static class Builder {

        private int x, y;
        private int width, height;
        private float scaleFactor;
        private boolean hasSubcomponents = true;

        private Builder() {
        }

        private static boolean hasSubcomponents(int value) {
            return Math.floorMod(value, COARSENESS_PARAMETER) == 0;
        }

        @Contract("_ -> this")
        public Builder setX(int x) {
            if (hasSubcomponents(x)) this.x = x;
            else hasSubcomponents = false;
            return this;
        }

        @Contract("_ -> this")
        public Builder setY(int y) {
            if (hasSubcomponents(y)) this.y = y;
            else hasSubcomponents = false;
            return this;
        }

        @Contract("_ -> this")
        public Builder setHeight(int height) {
            if (hasSubcomponents(height)) this.height = height;
            else hasSubcomponents = false;
            return this;
        }

        @Contract("_ -> this")
        public Builder setWidth(int width) {
            if (hasSubcomponents(width)) this.width = width;
            else hasSubcomponents = false;
            return this;
        }

        @Contract("_ -> this")
        public Builder setScaleFactor(float scaleFactor) {
            this.scaleFactor = scaleFactor;
            return this;
        }

        public ComponentParameters build() {
            return new ComponentParameters(x, y, width, height, scaleFactor, hasSubcomponents);
        }
    }
}
