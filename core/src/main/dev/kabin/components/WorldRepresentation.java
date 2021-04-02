package dev.kabin.components;

import dev.kabin.components.worldmodel.ComponentArrayListPool;
import dev.kabin.components.worldmodel.IndexedSetPool;
import dev.kabin.entities.Entity;
import dev.kabin.entities.EntityCollectionProvider;
import dev.kabin.entities.Layer;
import dev.kabin.entities.impl.CollisionData;
import dev.kabin.util.collections.IndexedSet;
import dev.kabin.util.pools.objectpool.Borrowed;
import dev.kabin.util.shapes.primitive.RectInt;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;


public class WorldRepresentation<GroupType extends Enum<GroupType> & Layer, EntityType extends Entity<GroupType, ?, ?>> {

    public static final int
            AVAILABLE_ARRAYLISTS_OF_COMPONENT = 200,
            MAXIMAL_COMPONENT_SIZE = 512,
            AVAILABLE_COMPONENT_HASHSETS = 10_000,
            AVAILABLE_ENTITY_HASHSETS = 10_000;

    public static final Logger logger = Logger.getLogger(Component.class.getName());
    private final EntityCollectionProvider<GroupType, EntityType> entityCollectionProvider;
    private final IndexedSetPool<Component> componentIndexedSetPool = new IndexedSetPool<>(AVAILABLE_COMPONENT_HASHSETS, i -> new IndexedSet<>());
    private final IndexedSetPool<EntityType> entityIndexedSetPool = new IndexedSetPool<>(AVAILABLE_ENTITY_HASHSETS, i -> new IndexedSet<>());
    // Keep an object pool for ArrayList<Component> instances.
    private final ComponentArrayListPool componentArrayListPool = new ComponentArrayListPool(
            AVAILABLE_ARRAYLISTS_OF_COMPONENT, i -> new ArrayList<>(), List::clear
    );
    private final Component rootComponent;
    private long timeStampLastEntityWhereaboutsRegistered = Long.MIN_VALUE;
    private ArrayList<EntityType> entitiesInCameraNeighborhoodCached;
    private final long entitiesInCameraNeighborhoodLastUpdated = Long.MIN_VALUE;
    private ArrayList<EntityType> entitiesInCameraBoundsCached;
    private long entitiesInCameraBoundsLastUpdated = Long.MIN_VALUE;
    private Map<EntityType, IndexedSet<Component>> entityToIndivisibleComponentMapping = new HashMap<>();
    private Map<Component, IndexedSet<EntityType>> indivisibleComponentToEntityMapping = new HashMap<>();

    public WorldRepresentation(Class<GroupType> entityGroups, int width, int height, float scaleFactor) {
        entityCollectionProvider = new EntityCollectionProvider<>(entityGroups);
        rootComponent = makeRepresentationOf(width, height, scaleFactor);
    }

    @NotNull
    @Contract("_, _, _ -> new")
    private static Component makeRepresentationOf(int width, int height, float scaleFactor) {
        int x = MAXIMAL_COMPONENT_SIZE, y = MAXIMAL_COMPONENT_SIZE;
        while (x < width * 2 || y < height * 2) {
            x *= 2;
            y *= 2;
        }
        logger.log(Level.WARNING, "Creating components with dimensions {" + x + ", " + y + "}");
        return Component.make(ComponentParameters.builder().setX(-x / 2).setY(-y / 2).setWidth(x).setHeight(y).setScaleFactor(scaleFactor).build());
    }

    public void actionForEachEntityOrderedByType(Consumer<EntityType> renderEntityGlobalStateTime) {
        entityCollectionProvider.actionForEachEntityOrderedByGroup(renderEntityGlobalStateTime);
    }


    public List<EntityType> getEntitiesWithinCameraBoundsCached(RectInt cameraPosition) {
        if (entitiesInCameraBoundsLastUpdated <= timeStampLastEntityWhereaboutsRegistered) {
            entitiesInCameraBoundsLastUpdated = System.currentTimeMillis();
            return entitiesInCameraBoundsCached = getContainedEntities(cameraPosition);
        }
        return entitiesInCameraBoundsCached;
    }

    public void updateLocation(@NotNull EntityType entity) {
        updateLocation(entityToIndivisibleComponentMapping,
                indivisibleComponentToEntityMapping,
                entity,
                entity.graphicsNbd(),
                rootComponent);
    }

    /**
     * If the neighborhood of the given entity intersects this component,
     * then the entity will be added to this components entity list. Otherwise, it is removed.
     * A recursive call is made to all of this components sub-components method.
     *
     * @param entity the entity whose whereabouts are stored.
     */
    private void updateLocation(
            Map<EntityType, IndexedSet<Component>> entityToIndivisibleComponentMapping,
            Map<Component, IndexedSet<EntityType>> indivisibleComponentToEntityMapping,
            @NotNull EntityType entity,
            /* Caching the below calculation makes a big difference.*/
            @NotNull RectInt cachedEntityNodeNeighborhood,
            @NotNull Component component
    ) {
        if (component.getUnderlyingRectInt().meets(cachedEntityNodeNeighborhood)) {
            if (component.hasSubComponents()) {
                for (Component subComponent : component.getSubComponents()) {
                    updateLocation(entityToIndivisibleComponentMapping,
                            indivisibleComponentToEntityMapping,
                            entity,
                            cachedEntityNodeNeighborhood,
                            subComponent);
                }
            } else {
                entityToIndivisibleComponentMapping.computeIfAbsent(
                        entity,
                        c -> componentIndexedSetPool.borrow()
                ).add(component);

                indivisibleComponentToEntityMapping.computeIfAbsent(
                        component,
                        c -> entityIndexedSetPool.borrow()
                ).add(entity);
            }
        }
    }

    /**
     * Finds list of entities which are are in the given component such that their nbd meets the given nbd.
     */
    @NotNull
    private ArrayList<EntityType> getContainedEntities(@NotNull RectInt neighborhood) {
        ArrayList<Component> treeSearchResult = treeSearchFindIndivisibleComponentsMatching(
                c -> c.getUnderlyingRectInt().meets(neighborhood)
        );

        // TODO: deal with this new.
        final ArrayList<EntityType> containedEntities = new ArrayList<>();

        final IndexedSet<EntityType> setOfEntitiesToReturn = entityIndexedSetPool.borrow();
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0, n = treeSearchResult.size(); i < n; i++) {

            if (treeSearchResult.get(i) == null) continue;

            IndexedSet<EntityType> entitiesInComponent = indivisibleComponentToEntityMapping.get(treeSearchResult.get(i));

            // Goto next iteration
            if (entitiesInComponent == null) continue;

            for (int j = 0, m = entitiesInComponent.size(); j < m; j++) {
                final EntityType entityToAdd = entitiesInComponent.get(j);
                // Finds unique entities to return.
                if (setOfEntitiesToReturn.add(entityToAdd)) {
                    containedEntities.add(entityToAdd);
                }
            }

        }
        componentArrayListPool.giveBack(treeSearchResult);
        entityIndexedSetPool.giveBack(setOfEntitiesToReturn);


        logger.log(Level.FINE, "A call to getContainedEntities() returned " + containedEntities.size()
                + " entities.");
        return containedEntities;
    }

    public void forEachEntityInCameraNeighborhood(Consumer<EntityType> action) {
        ArrayList<EntityType> entities = entitiesInCameraNeighborhoodCached;
        if (entities == null) return;
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0, n = entities.size(); i < n; i++) {
            action.accept(entities.get(i));
        }
    }

    /**
     * Registers the whereabouts of all entities from the given list.
     *
     * @param region Each entity found inside this region gets its whereabouts updated.
     */
    public void registerEntityWhereabouts(RectInt region) {

        // Free resources from previous iteration.
        {

            // Give back data to pools.
            componentIndexedSetPool.giveBackAll();
            if (componentIndexedSetPool.taken() > 0) {
                throw new RuntimeException("After freeing resources, some were still marked as taken. There were so many: " + componentIndexedSetPool.taken());
            }


            entityIndexedSetPool.giveBackAll();
            if (entityIndexedSetPool.taken() > 0) {
                throw new RuntimeException("After freeing resources, some were still marked as taken. There were so many: " + entityIndexedSetPool.taken());
            }
        }


        final Map<EntityType, IndexedSet<Component>> entityToIndivisibleComponentMapping = new HashMap<>();
        final Map<Component, IndexedSet<EntityType>> indivisibleComponentToEntityMapping = new HashMap<>();

        entityCollectionProvider.actionForEachEntityOrderedByGroup(entity ->
                updateLocation(entityToIndivisibleComponentMapping, // Should NOT be this.entityToIndivisibleComponentMapping
                        indivisibleComponentToEntityMapping,  // Should NOT be this.indivisibleComponentToEntityMapping
                        entity,
                        entity.graphicsNbd(),
                        rootComponent
                ));

        // Update references; keep the data ready to be cleared around until the beginning of the next iteration.
        this.entityToIndivisibleComponentMapping = entityToIndivisibleComponentMapping;
        this.indivisibleComponentToEntityMapping = indivisibleComponentToEntityMapping;


        entitiesInCameraNeighborhoodCached = getContainedEntities(region);
        Collections.sort(entitiesInCameraNeighborhoodCached);


        timeStampLastEntityWhereaboutsRegistered = System.currentTimeMillis();
    }

    @Borrowed(origin = "SEARCH_ALG_OBJECT_POOL")
    public @NotNull ArrayList<Component> treeSearchFindIndivisibleComponentsMatching(
            Predicate<Component> condition
    ) {
        final ArrayList<Component> matches = componentArrayListPool.borrow();
        final ArrayList<Component> layer = componentArrayListPool.borrow();
        layer.add(rootComponent);
        treeSearchRecursionStep(matches, layer, condition);
        componentArrayListPool.giveBackAllExcept(matches);
        if (componentArrayListPool.taken() != 1)
            throw new RuntimeException("No of taken: " + componentArrayListPool.taken());
        return matches;
    }

    private void treeSearchRecursionStep(
            @NotNull ArrayList<Component> matches,
            @NotNull ArrayList<Component> layer,
            @NotNull Predicate<Component> condition
    ) {
        if (layer.isEmpty()) {
            return;
        }
        if (layer.get(0).hasSubComponents()) {
            final ArrayList<Component> newLayer = componentArrayListPool.borrow();
            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < layer.size(); i++) {
                layer.get(i).forEachMatching(newLayer::add, condition);
            }
            treeSearchRecursionStep(matches, newLayer, condition);
        } else {
            layer.removeIf(condition.negate());
            matches.addAll(layer);
        }
    }


    public void clearUnusedData(@NotNull RectInt rect) {
        final ArrayList<Component> treeSearchResult = treeSearchFindIndivisibleComponentsMatching(
                c -> !c.getUnderlyingRectInt().meets(rect)
        );
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0, n = treeSearchResult.size(); i < n; i++) {
            final Component c = treeSearchResult.get(i);
            if (c.isActive() && indivisibleComponentToEntityMapping.containsKey(c)) {
                c.clearData();
                c.setActive(false);
            }
        }
        componentArrayListPool.giveBack(treeSearchResult);
    }

    public void loadNearbyData(@NotNull RectInt rect) {
        final ArrayList<Component> treeSearchResult = treeSearchFindIndivisibleComponentsMatching(
                c -> c.getUnderlyingRectInt().meets(rect)
        );
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0, n = treeSearchResult.size(); i < n; i++) {
            final Component c = treeSearchResult.get(i);
            if (c.isInactive() && indivisibleComponentToEntityMapping.containsKey(c)) {

                final IndexedSet<EntityType> entities = indivisibleComponentToEntityMapping.get(c);
                for (int j = 0, m = entities.size(); j < m; j++) {
                    final EntityType entity = entities.get(j);
                    if (entity instanceof CollisionData) {
                        ((CollisionData) entity).actionEachCollisionPoint(c::incrementCollisionAt);
                    }
                }

                c.setActive(true);
            }
        }
        componentArrayListPool.giveBack(treeSearchResult);
    }


    public void clearData() {
        rootComponent.clearData();
    }

    public void activate(int x, int y) {
        rootComponent.activate(x, y);
    }

    public void incrementCollisionAt(int x, int y) {
        rootComponent.incrementCollisionAt(x, y);
    }

    public void decrementCollisionAt(int x, int y) {
        rootComponent.decrementCollisionAt(x, y);
    }

    public boolean isCollisionAt(int x, int y) {
        return rootComponent.isCollisionAt(x, y);
    }

    public float getVectorFieldX(int x, int y) {
        return rootComponent.getVectorFieldX(x, y);
    }

    public float getVectorFieldY(int x, int y) {
        return rootComponent.getVectorFieldY(x, y);
    }

    public boolean isLadderAt(int x, int y) {
        return rootComponent.isLadderAt(x, y);
    }

    public int getCollision(int x, int y) {
        return rootComponent.getCollision(x, y);
    }

    public boolean unregisterEntity(EntityType e) {
        return entityCollectionProvider.unregisterEntity(e);
    }

    public void registerEntity(EntityType e) {
        entityCollectionProvider.registerEntity(e);
    }

    public void populateCollection(Collection<EntityType> allEntities,
                                   Predicate<EntityType> criterion) {
        entityCollectionProvider.populateCollection(allEntities, criterion);
    }

    public void sortAllLayers() {
        entityCollectionProvider.sortAllLayers();
    }

    public int getWorldSizeX() {
        return rootComponent.getWidth();
    }

    public int getWorldSizeY() {
        return rootComponent.getHeight();
    }
}
