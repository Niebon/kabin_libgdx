package dev.kabin.kinetics;

import dev.kabin.util.collections.LazyList;
import dev.kabin.util.geometry.polygon.Triangle;

public interface Body {

    float x();

    float y();

    float m();

    float vx();

    float vy();

    LazyList<Triangle> triangulation();

}
