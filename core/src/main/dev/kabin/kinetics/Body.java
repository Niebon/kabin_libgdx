package dev.kabin.kinetics;

import dev.kabin.util.geometry.ModifiablePolygon;

public interface Body {

    float x();

    float y();

    float m();

    float vx();

    float vy();

    ModifiablePolygon representation();

}
