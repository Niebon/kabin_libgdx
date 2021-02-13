package dev.kabin.components;

import dev.kabin.collections.IndexedSet;
import dev.kabin.components.worldmodel.ComponentArrayListPool;
import dev.kabin.components.worldmodel.IndexedSetPool;
import dev.kabin.entities.CollisionData;
import dev.kabin.entities.Entity;
import dev.kabin.entities.EntityCollectionProvider;
import dev.kabin.utilities.functioninterfaces.PrimitiveIntPairConsumer;
import dev.kabin.utilities.pools.objectpool.Borrowed;
import dev.kabin.utilities.shapes.primitive.MutableRectInt;
import dev.kabin.utilities.shapes.primitive.RectInt;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;


public class WorldRepresentation implements Entity.PhysicsParameters{

    public static final int
            AVAILABLE_ARRAYLISTS_OF_COMPONENT = 200,
            MAXIMAL_COMPONENT_SIZE = 512,
            AVAILABLE_COMPONENT_HASHSETS = 10_000,
            AVAILABLE_ENTITY_HASHSETS = 10_000;

    public static final Logger logger = Logger.getLogger(Component.class.getName());



    private long timeStampLastEntityWhereaboutsRegistered = Long.MIN_VALUE;
    private List<Entity> entitiesInCameraNeighborhoodCached;
    private long entitiesInCameraNeighborhoodLastUpdated = Long.MIN_VALUE;
    private List<Entity> entitiesInCameraBoundsCached;
    private long entitiesInCameraBoundsLastUpdated = Long.MIN_VALUE;
    private Map<Entity, IndexedSet<Component>> entityToIndivisibleComponentMapping = new HashMap<>();
    private Map<Component, IndexedSet<Entity>> indivisibleComponentToEntityMapping = new HashMap<>();




    private final IndexedSetPool<Component> COMPONENT_INDEXED_SET_POOL = new IndexedSetPool<>(AVAILABLE_COMPONENT_HASHSETS, i -> new IndexedSet<>());
    private final IndexedSetPool<Entity> ENTITY_INDEXED_SET_POOL = new IndexedSetPool<>(AVAILABLE_ENTITY_HASHSETS, i -> new IndexedSet<>());
    // Keep an object pool for ArrayList<Component> instances.
    private final ComponentArrayListPool SEARCH_ALG_OBJECT_POOL = new ComponentArrayListPool(
            AVAILABLE_ARRAYLISTS_OF_COMPONENT, i -> new ArrayList<>(), List::clear
    );

    private final Component rootComponent;

    public WorldRepresentation(int width, int height, float scaleFactor){
        rootComponent = makeRepresentationOf(width, height, scaleFactor);
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
        updateLocation(entityToIndivisibleComponentMapping, indivisibleComponentToEntityMapping,
                entity, entity.graphicsNbd(), rootComponent);
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
            @NotNull MutableRectInt cachedEntityNodeNeighborhood,
            @NotNull Component component
    ) {
        if (component.getUnderlyingRectInt().meets(cachedEntityNodeNeighborhood)) {
            if (component.hasSubComponents()) {
                for (Component subComponent : component.getSubComponents()) {
                    updateLocation(entityToIndivisibleComponentMapping,
                            indivisibleComponentToEntityMapping,
                            entity, cachedEntityNodeNeighborhood,
                            subComponent);
                }
            } else {
                entityToIndivisibleComponentMapping.computeIfAbsent(
                        entity,
                        c -> COMPONENT_INDEXED_SET_POOL.borrow()
                ).add(component);

                indivisibleComponentToEntityMapping.computeIfAbsent(
                        component,
                        c -> ENTITY_INDEXED_SET_POOL.borrow()
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


        final IndexedSet<Entity> setOfEntitiesToReturn = ENTITY_INDEXED_SET_POOL.borrow();
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
        SEARCH_ALG_OBJECT_POOL.giveBack(treeSearchResult);
        ENTITY_INDEXED_SET_POOL.giveBack(setOfEntitiesToReturn);


        logger.log(Level.FINE, "A call to getContainedEntities() returned " + containedEntities.size()
                + " entities.");
        return containedEntities;
    }



    /**
     * Registers the whereabouts of all entities from the given list.
     * This updates the information returned by
     */
    public void registerEntityWhereabouts() {

    	/*
    	Free resources from previous iteration.
    	 */
        {

            // Give back data to pools.
            COMPONENT_INDEXED_SET_POOL.giveBackAll();
            if (COMPONENT_INDEXED_SET_POOL.taken() > 0) {
                throw new RuntimeException("After freeing resources, some were still marked as taken. There were so many: " + COMPONENT_INDEXED_SET_POOL.taken());
            }


            ENTITY_INDEXED_SET_POOL.giveBackAll();
            if (ENTITY_INDEXED_SET_POOL.taken() > 0) {
                throw new RuntimeException("After freeing resources, some were still marked as taken. There were so many: " + ENTITY_INDEXED_SET_POOL.taken());
            }
        }


        final Map<Entity, IndexedSet<Component>> entityToIndivisibleComponentMapping = new HashMap<>();
        final Map<Component, IndexedSet<Entity>> indivisibleComponentToEntityMapping = new HashMap<>();

        EntityCollectionProvider.actionForEachEntityOrderedByType(e -> updateLocation(entityToIndivisibleComponentMapping,
                indivisibleComponentToEntityMapping,
                e,
                e.graphicsNbd(),
                rootComponent
        ));

        // Update static references; keep the data ready to be cleared around until the beginning of the next iteration.
        this.entityToIndivisibleComponentMapping = entityToIndivisibleComponentMapping;
        this.indivisibleComponentToEntityMapping = indivisibleComponentToEntityMapping;
        timeStampLastEntityWhereaboutsRegistered = System.currentTimeMillis();
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
        return new Component(new ComponentParameters().setX(-x / 2).setY(-y / 2).setWidth(x).setHeight(y).setScaleFactor(scaleFactor));
    }




    @Borrowed(origin = "SEARCH_ALG_OBJECT_POOL")
    public @NotNull ArrayList<Component> treeSearchFindIndivisibleComponentsMatching(
            Predicate<Component> condition
    ) {
        ArrayList<Component> matches = SEARCH_ALG_OBJECT_POOL.borrow();
        ArrayList<Component> layer = SEARCH_ALG_OBJECT_POOL.borrow();
        layer.add(rootComponent);
        treeSearchRecursionStep(matches, layer, condition);
        SEARCH_ALG_OBJECT_POOL.giveBackAllExcept(matches);
        if (SEARCH_ALG_OBJECT_POOL.taken() != 1)
            throw new RuntimeException("No of taken: " + SEARCH_ALG_OBJECT_POOL.taken());
        return matches;
    }

    private void treeSearchRecursionStep(
            ArrayList<Component> matches,
            @NotNull ArrayList<Component> layer,
            Predicate<Component> condition
    ) {
        if (layer.isEmpty()) {
            return;
        }
        if (layer.get(0).hasSubComponents()) {
            ArrayList<Component> newLayer = SEARCH_ALG_OBJECT_POOL.borrow();
            //noinspection ForLoopReplaceableByForEach
            for (int i = 0, n = layer.size(); i < n; i++) {
                for (Component child : layer.get(i).getSubComponents()) {
                    if (condition.test(child)) {
                        newLayer.add(child);
                    }
                }
            }
            treeSearchRecursionStep(matches, newLayer, condition);
        } else {
            layer.removeIf(condition.negate());
            matches.addAll(layer);
        }
    }


    public void clearUnusedData(@NotNull MutableRectInt rect) {
        final ArrayList<Component> treeSearchResult = treeSearchFindIndivisibleComponentsMatching(
                c -> !c.getUnderlyingRectInt().meets(rect)
        );
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0, n = treeSearchResult.size(); i < n; i++) {
            final Component c = treeSearchResult.get(i);
            if (c.isActive() && indivisibleComponentToEntityMapping.containsKey(c)) {
                c.clearData();
                c.setActive(true);
            }
        }
        SEARCH_ALG_OBJECT_POOL.giveBack(treeSearchResult);
    }

    public void loadNearbyData(@NotNull MutableRectInt rect) {
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
                        ((CollisionData) entity).actionEachCollisionPoint(new PrimitiveIntPairConsumer() {
                            @Override
                            public void accept(int x, int y) {
                                c.incrementCollisionAt(x, y);
                            }

                            @Override
                            public String toString() {
                                return "loadNearbyData: GlobalData.getRootComponent().incrementCollisionAt";
                            }
                        });
                    }
                }

                c.setActive(true);
            }
        }
        SEARCH_ALG_OBJECT_POOL.giveBack(treeSearchResult);
    }



    public void clearData(){
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
}