package dev.kabin.components;

import org.jetbrains.annotations.Contract;

import java.util.HashMap;

public record ComponentParameters(int x, int y, int width, int height, float scaleFactor, boolean hasSubcomponents) {

    public static final int COARSENESS_PARAMETER = 64; // 256;

    public static Builder builder() {
        return new Builder();
    }


    @Override
    public String toString() {
        return new HashMap<>() {
            {
                put("x", x());
                put("y", y());
                put("width", width());
                put("height", height());
                put("scaleFactor", scaleFactor());
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
