package dev.kabin.util.shapes;

import java.util.HashSet;
import java.util.Set;

@Deprecated
public abstract class GrowingDirectedRectBoxed<T extends Number & Comparable<T>> extends GrowingRectBoxed<T> implements DirectedOld {

    private final Set<DirectedOld> arrows = new HashSet<>();

    @Override
    public Set<DirectedOld> arrows() {
        return arrows;
    }

    public GrowingDirectedRectBoxed(GrowingDirectedRectBoxed<T> copy) {
        super(copy);
    }

    public GrowingDirectedRectBoxed() {
    }
}
