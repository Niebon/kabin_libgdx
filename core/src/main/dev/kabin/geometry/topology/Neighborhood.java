package dev.kabin.geometry.topology;


import dev.kabin.geometry.shapes.RectInt;

/**
 * Instances of this interface can be be thought of as points in the
 * box-topology on R^2. This interface offers a method which returns
 * a neighborhood of a point.
 */
public interface Neighborhood {

    RectInt getNeighborhood();

    default boolean meets(Neighborhood other) {
        return getNeighborhood().meets(other.getNeighborhood());
    }

    default boolean meets(RectInt nbd) {
        return getNeighborhood().meets(nbd);
    }

}
