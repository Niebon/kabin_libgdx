package dev.kabin.entities;


import dev.kabin.util.Functions;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Keeps inaccessible collections for each {@link T entity group}.
 * Instances of {@link Entity} may be {@link #registerEntity(Entity) registered} or {@link #unregisterEntity(Entity) unregistered}.
 * <p>
 * This class offers functions to perform procedure for each entity which is registered as a member of a collection.
 *
 * @see #actionForEachEntityOfClass(Enum, Consumer)
 * @see #actionForEachEntityOrderedByGroup(Consumer)
 */
public class EntityCollectionProvider<T extends Enum<T> & Layer, E extends Entity<T, ?, ?>> {

    private final T[] typesOrdered;
    private final Map<T, List<E>> collectionMap;
    private final Class<T> groupTypeClass;


    public EntityCollectionProvider(Class<T> groupTypeClass) {
        //noinspection unchecked
        typesOrdered = Arrays.stream(groupTypeClass.getEnumConstants())
                .sorted(Comparator.comparingInt(T::getLayer))
                .toArray(i -> (T[]) Array.newInstance(groupTypeClass, i));
        collectionMap = Arrays
                .stream(groupTypeClass.getEnumConstants())
                .collect(Collectors.toMap(
                        Function.identity(),
                        val -> new ArrayList<>(),
                        Functions::projectLeft,
                        () -> new EnumMap<>(groupTypeClass)
                ));
        this.groupTypeClass = groupTypeClass;
    }

    public void registerEntity(E e) {
        collectionMap.get(e.getGroupType()).add(e);
    }

    public boolean unregisterEntity(E e) {
        return collectionMap.get(e.getGroupType()).remove(e);
    }

    public void actionForEachEntityOfClass(T entityGroup, Consumer<E> action) {
        final List<E> entities = collectionMap.get(entityGroup);
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0, n = entities.size(); i < n; i++) {
            action.accept(entities.get(i));
        }
    }

    public void actionForEachEntityOrderedByGroup(Consumer<E> action) {
        for (T entityGroup : typesOrdered) {
            actionForEachEntityOfClass(entityGroup, action);
        }
    }

    /**
     * Populate the given collection by dev.kabin.entities matching the criterion such that no objects are created.
     *
     * @param collection the collection to be populated.
     * @param criterion  the criterion to match.
     */
    public void populateCollection(Collection<E> collection,
                                   Predicate<E> criterion) {
        for (T entityGroup : typesOrdered) {
            final List<E> list = collectionMap.get(entityGroup);
            //noinspection ForLoopReplaceableByForEach
            for (int i = 0, n = list.size(); i < n; i++) {
                if (criterion.test(list.get(i))) {
                    collection.add(list.get(i));
                }
            }
        }
    }

    public void sortAllLayers() {
        for (var type : groupTypeClass.getEnumConstants()) {
            sortByLayer(type);
        }
    }

    public void sortByLayer(T entityGroup) {
        Collections.sort(collectionMap.get(entityGroup));
    }

}
