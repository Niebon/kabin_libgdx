package dev.kabin.components.worldmodel;

import dev.kabin.util.cell.Cell;
import dev.kabin.util.pools.objectpool.AbstractObjectPool;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ComponentArrayListPool extends AbstractObjectPool<ArrayList<Cell>> {

    public ComponentArrayListPool(int objectsAvailable, Supplier<ArrayList<Cell>> mapper,
                                  Consumer<ArrayList<Cell>> clearDataProcedure) {
        super(objectsAvailable, mapper, clearDataProcedure);
    }

}
