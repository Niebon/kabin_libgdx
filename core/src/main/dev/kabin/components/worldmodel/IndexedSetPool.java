package dev.kabin.components.worldmodel;

import dev.kabin.collections.Id;
import dev.kabin.collections.IndexedSet;
import dev.kabin.util.pools.objectpool.AbstractObjectPool;

import java.util.function.IntFunction;

public class IndexedSetPool<Entry extends Id> extends AbstractObjectPool<IndexedSet<Entry>> {
    public IndexedSetPool(int objectsAvailable, IntFunction<IndexedSet<Entry>> mapper) {
        super(objectsAvailable, mapper, IndexedSet::clear);
    }
}
