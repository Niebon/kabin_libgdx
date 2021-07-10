package dev.kabin.kinetics;

import dev.kabin.util.geometry.PolygonModifiable;

public interface Body {

    float x();

    float y();

    float m();

    float vx();

    float vy();

    PolygonModifiable representation();

}
