package dev.kabin.components.worldmodel;

import dev.kabin.util.linalg.IntMatrix;
import dev.kabin.util.pools.objectpool.AbstractObjectPool;

import java.util.function.Supplier;


/**
 * Makes sure that int data objects are re-used, instead of being garbage collected.
 */
public class IntMatrixPool extends AbstractObjectPool<IntMatrix> {


    public IntMatrixPool(int objectsAvailable, Supplier<IntMatrix> mapper) {
        super(objectsAvailable, mapper, IntMatrix::clear);
    }

}
