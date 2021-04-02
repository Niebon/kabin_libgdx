package dev.kabin.entities.impl;

import dev.kabin.entities.Layer;

public enum EntityGroup implements Layer {
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

    EntityGroup(int layer) {
        this.layer = layer;
    }

    @Override
    public int getLayer() {
        return layer;
    }

    @Override
    public String toString() {
        return name();
    }

}
