package dev.kabin.components.worldmodel;

import dev.kabin.util.linalg.FloatMatrix;
import dev.kabin.util.pools.objectpool.AbstractObjectPool;

import java.util.Arrays;
import java.util.function.Supplier;


/**
 * Makes sure that float data objects are re-used, instead of being garbage collected.
 */
public class FloatMatrixPool extends AbstractObjectPool<FloatMatrix> {

    public FloatMatrixPool(int objectsAvailable, Supplier<FloatMatrix> mapper) {
        super(objectsAvailable, mapper, m -> Arrays.fill(m.data(), 0));
    }

}
