package dev.kabin.kinetics;

import dev.kabin.util.collections.LazyList;
import dev.kabin.util.shapes.deltacomplexes.Simplex2;

public interface Body {

    float x();

    float y();

    float m();

    float vx();

    float vy();

    LazyList<Simplex2> triangulation();

}
