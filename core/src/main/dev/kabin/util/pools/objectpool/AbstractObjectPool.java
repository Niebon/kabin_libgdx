package dev.kabin.util.pools.objectpool;

import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * The implementor is responsible for returning objects.
 *
 * @param <ObjectType>
 */
public class AbstractObjectPool<ObjectType> {

    // Data holder and status.
    private final ObjectType[] objectHolder;
    private final Consumer<ObjectType> clearDataProcedure;
    private int nextFreeIndex = 0;


    public AbstractObjectPool(int objectsAvailable, Supplier<ObjectType> mapper,
                              Consumer<ObjectType> clearDataProcedure) {
        //noinspection unchecked
        objectHolder = (ObjectType[]) IntStream
                .range(0, objectsAvailable)
                .mapToObj(i -> mapper.get())
                .toArray(Object[]::new);
        this.clearDataProcedure = clearDataProcedure;
    }

    public int remaining() {
        return objectHolder.length - nextFreeIndex;
    }

    public int taken() {
        return nextFreeIndex;
    }

    public ObjectType borrow() throws IllegalArgumentException {
        return objectHolder[nextFreeIndex++];
    }

    public void giveBack(ObjectType data) throws IllegalArgumentException {
        int indexOfGivenData = -1;
        for (int i = 0, n = objectHolder.length; i < n; i++) {
            if (data == objectHolder[i]) {
                indexOfGivenData = i;
                break;
            }
        }
        if (indexOfGivenData >= 0) {
            nextFreeIndex = nextFreeIndex - 1;

            // Swap
            objectHolder[indexOfGivenData] = objectHolder[nextFreeIndex];
            objectHolder[nextFreeIndex] = data;
            clearDataProcedure.accept(data);
        }
    }

    public void giveBackAll() {
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0, n = objectHolder.length; i < n; i++) {
            clearDataProcedure.accept(objectHolder[i]);
        }
        nextFreeIndex = 0;
    }

    public void giveBackAllExcept(ObjectType data) {
        int indexOfGivenData = -1;
        for (int i = 0, n = objectHolder.length; i < n; i++) {
            if (data == objectHolder[i]) {
                indexOfGivenData = i;
            } else {
                clearDataProcedure.accept(objectHolder[i]);
            }
        }
        if (indexOfGivenData >= 0) {
            // Swap
            objectHolder[indexOfGivenData] = objectHolder[0];
            objectHolder[0] = data;
            nextFreeIndex = 1;
        }
    }



}
