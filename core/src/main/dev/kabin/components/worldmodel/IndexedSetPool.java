package dev.kabin.components.worldmodel;

import dev.kabin.util.collections.Id;
import dev.kabin.util.collections.IndexedSet;
import dev.kabin.util.pools.objectpool.AbstractObjectPool;

import java.util.function.Supplier;

public class IndexedSetPool<Entry extends Id> extends AbstractObjectPool<IndexedSet<Entry>> {
    public IndexedSetPool(int objectsAvailable, Supplier<IndexedSet<Entry>> mapper) {
        super(objectsAvailable, mapper, IndexedSet::clear);
    }
}
