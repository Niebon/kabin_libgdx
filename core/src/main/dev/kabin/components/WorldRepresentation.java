package dev.kabin.components;

import dev.kabin.collections.IndexedSet;
import dev.kabin.components.worldmodel.ComponentArrayListPool;
import dev.kabin.components.worldmodel.IndexedSetPool;
import dev.kabin.entities.impl.CollisionData;
import dev.kabin.entities.impl.Entity;
import dev.kabin.entities.impl.EntityCollectionProvider;
import dev.kabin.util.pools.objectpool.Borrowed;
import dev.kabin.util.shapes.primitive.RectInt;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;


public class WorldRepresentation {

    public static final int
            AVAILABLE_ARRAYLISTS_OF_COMPONENT = 200,
            MAXIMAL_COMPONENT_SIZE = 512,
            AVAILABLE_COMPONENT_HASHSETS = 10_000,
            AVAILABLE_ENTITY_HASHSETS = 10_000;

    public static final Logger LOGGER = Logger.getLogger(Component.class.getName());
    private final EntityCollectionProvider entityCollectionProvider = new EntityCollectionProvider();
    private final IndexedSetPool<Component> componentIndexedSetPool = new IndexedSetPool<>(AVAILABLE_COMPONENT_HASHSETS, i -> new IndexedSet<>());
    private final IndexedSetPool<Entity> entityIndexedSetPool = new IndexedSetPool<>(AVAILABLE_ENTITY_HASHSETS, i -> new IndexedSet<>());
    // Keep an object pool for ArrayList<Component> instances.
    private final ComponentArrayListPool componentArrayListPool = new ComponentArrayListPool(
            AVAILABLE_ARRAYLISTS_OF_COMPONENT, i -> new ArrayList<>(), List::clear
    );
    private final Component rootComponent;
    private long timeStampLastEntityWhereaboutsRegistered = Long.MIN_VALUE;
    private List<Entity> entitiesInCameraNeighborhoodCached;
    private long entitiesInCameraNeighborhoodLastUpdated = Long.MIN_VALUE;
    private List<Entity> entitiesInCameraBoundsCached;
    private long entitiesInCameraBoundsLastUpdated = Long.MIN_VALUE;
    private Map<Entity, IndexedSet<Component>> entityToIndivisibleComponentMapping = new HashMap<>();
    private Map<Component, IndexedSet<Entity>> indivisibleComponentToEntityMapping = new HashMap<>();

    public WorldRepresentation(int width, int height, float scaleFactor) {
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
        LOGGER.log(Level.WARNING, "Creating components with dimensions {" + x + ", " + y + "}");
        return Component.make(ComponentParameters.make().setX(-x / 2).setY(-y / 2).setWidth(x).setHeight(y).setScaleFactor(scaleFactor));
    }

    public void actionForEachEntityOrderedByType(Consumer<Entity> renderEntityGlobalStateTime) {
        entityCollectionProvider.actionForEachEntityOrderedByType(renderEntityGlobalStateTime);
    }

    public List<Entity> getEntityInCameraNeighborhood(RectInt cameraPosition) {
        entitiesInCameraNeighborhoodLastUpdated = System.currentTimeMillis();
        return entitiesInCameraNeighborhoodCached = getContainedEntities(cameraPosition);
    }

    public List<Entity> getEntityInCameraNeighborhoodCached(RectInt cameraPosition) {
        if (entitiesInCameraNeighborhoodLastUpdated <= timeStampLastEntityWhereaboutsRegistered) {
            entitiesInCameraNeighborhoodLastUpdated = System.currentTimeMillis();
            return entitiesInCameraNeighborhoodCached = getContainedEntities(cameraPosition);
        }
        return entitiesInCameraNeighborhoodCached;
    }

    public List<Entity> getEntitiesWithinCameraBoundsCached(RectInt cameraPosition) {
        if (entitiesInCameraBoundsLastUpdated <= timeStampLastEntityWhereaboutsRegistered) {
            entitiesInCameraBoundsLastUpdated = System.currentTimeMillis();
            return entitiesInCameraBoundsCached = getContainedEntities(cameraPosition);
        }
        return entitiesInCameraBoundsCached;
    }

    public void updateLocation(@NotNull Entity entity) {
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
            Map<Entity, IndexedSet<Component>> entityToIndivisibleComponentMapping,
            Map<Component, IndexedSet<Entity>> indivisibleComponentToEntityMapping,
            @NotNull Entity entity,
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
    public List<Entity> getContainedEntities(@NotNull RectInt neighborhood) {
        ArrayList<Component> treeSearchResult = treeSearchFindIndivisibleComponentsMatching(
                c -> c.getUnderlyingRectInt().meets(neighborhood)
        );

        // TODO: deal with this new.
        final List<Entity> containedEntities = new ArrayList<>();

        final IndexedSet<Entity> setOfEntitiesToReturn = entityIndexedSetPool.borrow();
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0, n = treeSearchResult.size(); i < n; i++) {

            if (treeSearchResult.get(i) == null) continue;

            IndexedSet<Entity> entitiesInComponent = indivisibleComponentToEntityMapping.get(treeSearchResult.get(i));

            // Goto next iteration
            if (entitiesInComponent == null) continue;

            for (int j = 0, m = entitiesInComponent.size(); j < m; j++) {
                final Entity entityToAdd = entitiesInComponent.get(j);
                // Finds unique entities to return.
                if (setOfEntitiesToReturn.add(entityToAdd)) {
                    containedEntities.add(entityToAdd);
                }
            }

        }
        componentArrayListPool.giveBack(treeSearchResult);
        entityIndexedSetPool.giveBack(setOfEntitiesToReturn);


        LOGGER.log(Level.FINE, "A call to getContainedEntities() returned " + containedEntities.size()
                + " entities.");
        return containedEntities;
    }

    /**
     * Registers the whereabouts of all entities from the given list.
     * This updates the information returned by
     */
    public void registerEntityWhereabouts() {

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


        final Map<Entity, IndexedSet<Component>> entityToIndivisibleComponentMapping = new HashMap<>();
        final Map<Component, IndexedSet<Entity>> indivisibleComponentToEntityMapping = new HashMap<>();

        entityCollectionProvider.actionForEachEntityOrderedByType(entity ->
                updateLocation(entityToIndivisibleComponentMapping, // Should NOT be this.entityToIndivisibleComponentMapping
                        indivisibleComponentToEntityMapping,  // Should NOT be this.indivisibleComponentToEntityMapping
                        entity,
                        entity.graphicsNbd(),
                        rootComponent
                ));

        // Update references; keep the data ready to be cleared around until the beginning of the next iteration.
        this.entityToIndivisibleComponentMapping = entityToIndivisibleComponentMapping;
        this.indivisibleComponentToEntityMapping = indivisibleComponentToEntityMapping;
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

                final IndexedSet<Entity> entities = indivisibleComponentToEntityMapping.get(c);
                for (int j = 0, m = entities.size(); j < m; j++) {
                    final Entity entity = entities.get(j);
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

    public boolean unregisterEntity(Entity e) {
        return entityCollectionProvider.unregisterEntity(e);
    }

    public void registerEntity(Entity e) {
        entityCollectionProvider.registerEntity(e);
    }

    public void populateCollection(Collection<Entity> allEntities, Predicate<Entity> criterion) {
        entityCollectionProvider.populateCollection(allEntities, criterion);
    }

    public void sortAllLayers(){
        entityCollectionProvider.sortAllLayers();
    }
}
