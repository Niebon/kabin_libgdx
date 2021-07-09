package dev.kabin.kinetics;

import dev.kabin.util.geometry.Polygon;

public interface Body {

    float x();

    float y();

    float m();

    float vx();

    float vy();

    Polygon representation();

}
