package dev.kabin.components;

import dev.kabin.components.worldmodel.ComponentArrayListPool;
import dev.kabin.components.worldmodel.IndexedSetPool;
import dev.kabin.entities.Entity;
import dev.kabin.entities.EntityCollectionProvider;
import dev.kabin.entities.Layer;
import dev.kabin.entities.libgdximpl.CollisionData;
import dev.kabin.util.cell.Cell;
import dev.kabin.util.collections.IndexedSet;
import dev.kabin.util.pools.objectpool.Borrowed;
import dev.kabin.util.shapes.primitive.RectInt;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;


public class WorldRepresentation<GroupType extends Enum<GroupType> & Layer, EntityType extends Entity<GroupType, ?, ?>> {

    public static final int
            POOL_OBJECTS_AVAILABLE = 128,
            INDIVISIBLE_COMPONENT_SIZE = 512,
            AVAILABLE_ARRAYLISTS_OF_COMPONENT = 200,
            AVAILABLE_COMPONENT_HASHSETS = 10_000,
            AVAILABLE_ENTITY_HASHSETS = 10_000;

    public static final Logger logger = Logger.getLogger(Cell.class.getName());
    private final EntityCollectionProvider<GroupType, EntityType> entityCollectionProvider;
    private final IndexedSetPool<Cell> componentIndexedSetPool = new IndexedSetPool<>(AVAILABLE_COMPONENT_HASHSETS, IndexedSet::new);
    private final IndexedSetPool<EntityType> entityIndexedSetPool = new IndexedSetPool<>(AVAILABLE_ENTITY_HASHSETS, IndexedSet::new);
    // Keep an object pool for ArrayList<Component> instances.
    private final ComponentArrayListPool componentArrayListPool = new ComponentArrayListPool(
            AVAILABLE_ARRAYLISTS_OF_COMPONENT, ArrayList::new, List::clear
    );
    private final Cell rootCell;
    private final long entitiesInCameraNeighborhoodLastUpdated = Long.MIN_VALUE;
    private long timeStampLastEntityWhereaboutsRegistered = Long.MIN_VALUE;
    private ArrayList<EntityType> entitiesInCameraNeighborhoodCached;
    private ArrayList<EntityType> entitiesInCameraBoundsCached;
    private long entitiesInCameraBoundsLastUpdated = Long.MIN_VALUE;
    private Map<EntityType, IndexedSet<Cell>> entityToIndivisibleComponentMapping = new HashMap<>();
    private Map<Cell, IndexedSet<EntityType>> indivisibleComponentToEntityMapping = new HashMap<>();

    public WorldRepresentation(Class<GroupType> entityGroups, int width, int height) {
        entityCollectionProvider = new EntityCollectionProvider<>(entityGroups);
        rootCell = Cell.makeRepresentationOf(width, height, INDIVISIBLE_COMPONENT_SIZE, POOL_OBJECTS_AVAILABLE);
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
                rootCell);
    }

    /**
     * If the neighborhood of the given entity intersects this component,
     * then the entity will be added to this components entity list. Otherwise, it is removed.
     * A recursive call is made to all of this components sub-components method.
     *
     * @param entity the entity whose whereabouts are stored.
     */
    private void updateLocation(
            Map<EntityType, IndexedSet<Cell>> entityToIndivisibleComponentMapping,
            Map<Cell, IndexedSet<EntityType>> indivisibleComponentToEntityMapping,
            @NotNull EntityType entity,
            /* Caching the below calculation makes a big difference.*/
            @NotNull RectInt cachedEntityNodeNeighborhood,
            @NotNull Cell cell
    ) {
        if (cell.getUnderlyingRectInt().meets(cachedEntityNodeNeighborhood)) {
            if (cell.hasSubComponents()) {
                for (Cell subCell : cell.getSubComponents()) {
                    updateLocation(entityToIndivisibleComponentMapping,
                            indivisibleComponentToEntityMapping,
                            entity,
                            cachedEntityNodeNeighborhood,
                            subCell);
                }
            } else {
                entityToIndivisibleComponentMapping.computeIfAbsent(
                        entity,
                        c -> componentIndexedSetPool.borrow()
                ).add(cell);

                indivisibleComponentToEntityMapping.computeIfAbsent(
                        cell,
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
        ArrayList<Cell> treeSearchResult = treeSearchFindIndivisibleComponentsMatching(
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


        final Map<EntityType, IndexedSet<Cell>> entityToIndivisibleComponentMapping = new HashMap<>();
        final Map<Cell, IndexedSet<EntityType>> indivisibleComponentToEntityMapping = new HashMap<>();

        entityCollectionProvider.actionForEachEntityOrderedByGroup(entity ->
                updateLocation(entityToIndivisibleComponentMapping, // Should NOT be this.entityToIndivisibleComponentMapping
                        indivisibleComponentToEntityMapping,  // Should NOT be this.indivisibleComponentToEntityMapping
                        entity,
                        entity.graphicsNbd(),
                        rootCell
                ));

        // Update references; keep the data ready to be cleared around until the beginning of the next iteration.
        this.entityToIndivisibleComponentMapping = entityToIndivisibleComponentMapping;
        this.indivisibleComponentToEntityMapping = indivisibleComponentToEntityMapping;


        entitiesInCameraNeighborhoodCached = getContainedEntities(region);
        Collections.sort(entitiesInCameraNeighborhoodCached);


        timeStampLastEntityWhereaboutsRegistered = System.currentTimeMillis();
    }

    @Borrowed(origin = "SEARCH_ALG_OBJECT_POOL")
    public @NotNull ArrayList<Cell> treeSearchFindIndivisibleComponentsMatching(
            Predicate<Cell> condition
    ) {
        final ArrayList<Cell> matches = componentArrayListPool.borrow();
        final ArrayList<Cell> layer = componentArrayListPool.borrow();
        layer.add(rootCell);
        treeSearchRecursionStep(matches, layer, condition);
        componentArrayListPool.giveBackAllExcept(matches);
        if (componentArrayListPool.taken() != 1)
            throw new RuntimeException("No of taken: " + componentArrayListPool.taken());
        return matches;
    }

    private void treeSearchRecursionStep(
            @NotNull ArrayList<Cell> matches,
            @NotNull ArrayList<Cell> layer,
            @NotNull Predicate<Cell> condition
    ) {
        if (layer.isEmpty()) {
            return;
        }
        if (layer.get(0).hasSubComponents()) {
            final ArrayList<Cell> newLayer = componentArrayListPool.borrow();
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
        final ArrayList<Cell> treeSearchResult = treeSearchFindIndivisibleComponentsMatching(
                c -> !c.getUnderlyingRectInt().meets(rect)
        );
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0, n = treeSearchResult.size(); i < n; i++) {
            final Cell c = treeSearchResult.get(i);
            if (c.isActive() && indivisibleComponentToEntityMapping.containsKey(c)) {
                c.clearData();
                c.setActive(false);
            }
        }
        componentArrayListPool.giveBack(treeSearchResult);
    }

    public void loadNearbyData(@NotNull RectInt rect) {
        final ArrayList<Cell> treeSearchResult = treeSearchFindIndivisibleComponentsMatching(
                c -> c.getUnderlyingRectInt().meets(rect)
        );
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0, n = treeSearchResult.size(); i < n; i++) {
            final Cell c = treeSearchResult.get(i);
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
        rootCell.clearData();
    }

    public void activate(int x, int y) {
        rootCell.activate(x, y);
    }

    public void incrementCollisionAt(int x, int y) {
        rootCell.incrementCollisionAt(x, y);
    }

    public void decrementCollisionAt(int x, int y) {
        rootCell.decrementCollisionAt(x, y);
    }

    public boolean isCollisionAt(int x, int y) {
        return rootCell.isCollisionAt(x, y);
    }

    public float getVectorFieldX(int x, int y) {
        return rootCell.getVectorFieldX(x, y);
    }

    public float getVectorFieldY(int x, int y) {
        return rootCell.getVectorFieldY(x, y);
    }

    public boolean isLadderAt(int x, int y) {
        return rootCell.isLadderAt(x, y);
    }

    public int getCollision(int x, int y) {
        return rootCell.getCollision(x, y);
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
        return rootCell.getWidth();
    }

    public int getWorldSizeY() {
        return rootCell.getHeight();
    }
}
