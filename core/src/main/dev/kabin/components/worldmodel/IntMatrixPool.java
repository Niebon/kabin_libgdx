package dev.kabin.components.worldmodel;

import dev.kabin.util.linalg.IntMatrix;
import dev.kabin.util.pools.objectpool.AbstractObjectPool;

import java.util.function.IntFunction;

import static dev.kabin.components.ComponentParameters.COARSENESS_PARAMETER;

/**
 * Makes sure that int data objects are re-used, instead of being garbage collected.
 */
public class IntMatrixPool extends AbstractObjectPool<IntMatrix> {

    final static int OBJECTS_AVAILABLE = 64 * 2; // 2 because Ladder data & Collision data.

    private static final IntMatrixPool instance
            = new IntMatrixPool(OBJECTS_AVAILABLE, i -> new IntMatrix(COARSENESS_PARAMETER, COARSENESS_PARAMETER));

    IntMatrixPool(int objectsAvailable, IntFunction<IntMatrix> mapper) {
        super(objectsAvailable, mapper, IntMatrix::clear);
    }

    public static IntMatrixPool getInstance() {
        return instance;
    }
}
