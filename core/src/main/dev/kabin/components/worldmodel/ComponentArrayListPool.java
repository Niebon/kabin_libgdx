package dev.kabin.components.worldmodel;

import dev.kabin.components.Component;
import dev.kabin.util.pools.objectpool.AbstractObjectPool;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.IntFunction;

public class ComponentArrayListPool extends AbstractObjectPool<ArrayList<Component>> {

    public ComponentArrayListPool(int objectsAvailable, IntFunction<ArrayList<Component>> mapper,
                                  Consumer<ArrayList<Component>> clearDataProcedure) {
        super(objectsAvailable, mapper, clearDataProcedure);
    }

}
