package dev.kabin.util.cell;

import dev.kabin.components.worldmodel.FloatMatrixPool;
import dev.kabin.components.worldmodel.IntMatrixPool;

record CellParameters(int x,
                      int y,
                      int width,
                      int height,
                      float scaleFactor,
                      boolean hasSubCells,
                      int minimalCellSize,
                      IntMatrixPool intMatrixPool,
                      FloatMatrixPool floatMatrixPool) {

    static Builder builder(int minimalCellSize) {
        return new Builder(minimalCellSize);
    }

    static class Builder {

        private int x, y;
        private int width, height;
        private float scaleFactor;
        private boolean hasSubCells = true;
        private final int minimalCellSize;
        private IntMatrixPool intMatrixPool;
        private FloatMatrixPool floatMatrixPool;

        private Builder(int minimalCellSize) {
            this.minimalCellSize = minimalCellSize;
        }

        private boolean hasSubCells(int value) {
            return Math.floorMod(value, minimalCellSize) == 0;
        }

        public Builder setX(int x) {
            if (hasSubCells(x)) this.x = x;
            else hasSubCells = false;
            return this;
        }

        public Builder setY(int y) {
            if (hasSubCells(y)) this.y = y;
            else hasSubCells = false;
            return this;
        }

        public Builder setHeight(int height) {
            if (hasSubCells(height)) this.height = height;
            else hasSubCells = false;
            return this;
        }

        public Builder setWidth(int width) {
            if (hasSubCells(width)) this.width = width;
            else hasSubCells = false;
            return this;
        }

        public Builder setScaleFactor(float scaleFactor) {
            this.scaleFactor = scaleFactor;
            return this;
        }

        public Builder setIntMatrixPool(IntMatrixPool intMatrixPool) {
            this.intMatrixPool = intMatrixPool;
            return this;
        }

        public Builder setFloatArrayPool(FloatMatrixPool floatMatrixPool) {
            this.floatMatrixPool = floatMatrixPool;
            return this;
        }

        public CellParameters build() {
            return new CellParameters(x, y, width, height, scaleFactor, hasSubCells, minimalCellSize, intMatrixPool, floatMatrixPool);
        }
    }
}
