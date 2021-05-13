package dev.kabin.entities;

/**
 * A class that implements this interface can return an {@code int} called layer.
 * Can be used when the implementing class has some stack-like behavior in which instances are layered on top of one another.
 */
public interface Layer {

    /**
     * @return the layer of this instance.
     */
    int layer();

}
