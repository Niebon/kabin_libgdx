package dev.kabin.components.worldmodel;

import dev.kabin.util.linalg.FloatMatrix;
import dev.kabin.util.pools.objectpool.AbstractObjectPool;

import java.util.Arrays;
import java.util.function.IntFunction;

import static dev.kabin.components.ComponentParameters.COARSENESS_PARAMETER;

/**
 * Makes sure that float data objects are re-used, instead of being garbage collected.
 */
public class FloatArrayPool extends AbstractObjectPool<FloatMatrix> {

    final static int OBJECTS_AVAILABLE = 64 * 2;  // 2 because vector field X & Y.

    private static final FloatArrayPool instance
            = new FloatArrayPool(OBJECTS_AVAILABLE, i -> new FloatMatrix(COARSENESS_PARAMETER, COARSENESS_PARAMETER));

    FloatArrayPool(int objectsAvailable, IntFunction<FloatMatrix> mapper) {
        super(objectsAvailable, mapper, m -> Arrays.fill(m.data(), 0));
    }

    public static FloatArrayPool getInstance() {
        return instance;
    }
}
