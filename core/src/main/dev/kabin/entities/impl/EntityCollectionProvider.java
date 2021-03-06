package dev.kabin.entities.impl;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Keeps inaccessible collections for each {@link Type Type} at hand.
 * Instances of {@link Entity} may be {@link #registerEntity(Entity) registered} or {@link #unregisterEntity(Entity) unregistered}.
 * <p>
 * This class offers functions to perform procedure for each entity which is registered as a member of a collection.
 * See {@link #actionForEachEntityOfType(Type, Consumer)} and {@link #actionForEachEntityOrderedByType(Consumer)}.
 */
public class EntityCollectionProvider {

    private static final Type[] TYPES_ORDERED = Arrays.stream(Type.values())
            .sorted(Comparator.comparingInt(Type::getLayer))
            .toArray(Type[]::new);
    private final Map<Type, List<Entity>> collectionMap;

    public EntityCollectionProvider() {
        collectionMap = Arrays
                .stream(Type.values())
                .collect(Collectors.toMap(Function.identity(), val -> new ArrayList<>()));
    }

    public void registerEntity(Entity e) {
        collectionMap.get(e.getType().groupType()).add(e);
    }

    public boolean unregisterEntity(Entity e) {
        return collectionMap.get(e.getType().groupType()).remove(e);
    }

    public void actionForEachEntityOfType(Type type, Consumer<Entity> action) {
        final List<Entity> entities = collectionMap.get(type);
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0, n = entities.size(); i < n; i++) {
            action.accept(entities.get(i));
        }
    }

    public void actionForEachEntityOrderedByType(Consumer<Entity> action) {
        for (Type type : TYPES_ORDERED) {
            actionForEachEntityOfType(type, action);
        }
    }

    /**
     * Populate the given collection by dev.kabin.entities matching the criterion such that no objects are created.
     *
     * @param collection the collection to be populated.
     * @param criterion  the criterion to match.
     */
    public void populateCollection(Collection<Entity> collection, Predicate<Entity> criterion) {
        for (Type type : TYPES_ORDERED) {
            final List<Entity> list = collectionMap.get(type);
            //noinspection ForLoopReplaceableByForEach
            for (int i = 0, n = list.size(); i < n; i++) {
                if (criterion.test(list.get(i))) {
                    collection.add(list.get(i));
                }
            }
        }
    }

    public void sortAllLayers() {
        for (var type : Type.values()) {
            sortByLayer(type);
        }
    }

    public void sortByLayer(Type type) {
        Collections.sort(collectionMap.get(type));
    }

    public enum Type {
        SKY(-5),
        CLOUDS_LAYER_2(-4),
        CLOUDS(-4),
        STATIC_BACKGROUND(-3),
        BACKGROUND_LAYER_2(-2),
        BACKGROUND(-1),
        FOCAL_POINT(0),
        GROUND(1),
        FOREGROUND(2);

        private final int layer;

        Type(int layer) {
            this.layer = layer;
        }

        public int getLayer() {
            return layer;
        }

        @Override
        public String toString() {
            return name();
        }

    }
}