package dev.kabin.util.shapes;

import java.util.HashSet;
import java.util.Set;

public abstract class GrowingDirectedRectBoxed<T extends Number & Comparable<T>> extends GrowingRectBoxed<T> implements Directed {

    private final Set<Directed> arrows = new HashSet<>();

    @Override
    public Set<Directed> arrows() {
        return arrows;
    }

    public GrowingDirectedRectBoxed(GrowingDirectedRectBoxed<T> copy) {
        super(copy);
    }

    public GrowingDirectedRectBoxed() {
    }
}
