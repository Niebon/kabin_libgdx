package dev.kabin.util.cache;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public final class CachedCalculation<K, V> {


    private V reference = null;
    private final Function<K, V> recalculate;

    public CachedCalculation(@NotNull Function<K, V> recalculate) {
        this.recalculate = recalculate;
    }

    public void update(K params){
        reference = recalculate.apply(params);
    }

    public V updateAndGet(K params) {
        update(params);
        return reference;
    }

    public V get(K val) {
        return reference;
    }

}
