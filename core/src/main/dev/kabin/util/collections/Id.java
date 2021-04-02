package dev.kabin.util.collections;

/**
 * Slap this on top of classes whose instances would benefit iterating over using {@link IndexedSet}.
 */
public interface Id {

    /**
     * @return a unique id identifying this instance.
     */
    int getId();
}
