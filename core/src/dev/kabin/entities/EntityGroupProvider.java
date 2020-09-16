package dev.kabin.entities;

public class EntityGroupProvider {

    public enum Type {
        SKY(-5),
        CLOUDS_LAYER_2(-4),
        CLOUDS(-4),
        STATIC_BACKGROUND(-3),
        BACKGROUND_LAYER_2(-2),
        BACKGROUND(-1),
        FOCAL_POINT(0),
        GROUND(1),
        FOREGROUND(2);

        private final int layer;

        Type(int layer) {
            this.layer = layer;
        }

        public int getLayer() {
            return layer;
        }

        @Override
        public String toString() {
            return name();
        }
    }
}
