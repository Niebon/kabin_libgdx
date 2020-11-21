package dev.kabin.utilities.shapes;

import java.util.Set;

public interface Directed{

    Set<Directed> arrows();

    default void addArrow(Directed a){
        arrows().add(a);
    }

    default void addBiArrow(Directed a) {
        addArrow(a);
        a.addArrow(this);
    }
}
