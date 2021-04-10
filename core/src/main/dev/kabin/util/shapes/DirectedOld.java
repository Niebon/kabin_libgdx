package dev.kabin.util.shapes;

import java.util.Set;

@Deprecated
public interface DirectedOld {

    Set<DirectedOld> arrows();

    default void addArrow(DirectedOld a) {
        arrows().add(a);
    }

    default void addBiArrow(DirectedOld a) {
        addArrow(a);
        a.addArrow(this);
    }
}
