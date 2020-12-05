package dev.kabin.components;


import dev.kabin.GlobalData;
import dev.kabin.collections.Id;
import dev.kabin.collections.IndexedSet;
import dev.kabin.entities.CollisionData;
import dev.kabin.entities.Entity;
import dev.kabin.entities.EntityCollectionProvider;
import dev.kabin.utilities.Functions;
import dev.kabin.utilities.functioninterfaces.BiIntToFloatFunction;
import dev.kabin.utilities.functioninterfaces.FloatUnaryOperation;
import dev.kabin.utilities.functioninterfaces.IntBinaryOperator;
import dev.kabin.utilities.linalg.FloatMatrix;
import dev.kabin.utilities.linalg.IntMatrix;
import dev.kabin.utilities.pools.objectpool.AbstractObjectPool;
import dev.kabin.utilities.pools.objectpool.Borrowed;
import dev.kabin.utilities.shapes.RectFloat;
import dev.kabin.utilities.shapes.RectInt;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static dev.kabin.components.ComponentParameters.COARSENESS_PARAMETER;


/**
 * Represents a quadratic component which may consist of 4 quadratic sub-components:
 * <pre>
 * ___________________________
 * |            |            |
 * |            |            |
 * |     0      |     1      |
 * |____________|____________|
 * |            |            |
 * |     2      |     3      |
 * |            |            |
 * |____________|____________|
 * </pre>
 * This instance will have sub-components iff the provided {@link ComponentParameters} have {@link ComponentParameters#hasSubcomponents()}
 * equal to true.
 * <p>
 * In that case, we associate 0 -> (0,0), 1 -> (1,0), 2 -> (0,1), and 3 -> (1,1), where the pairs (m,n) determine the
 * position of the sub-component relative to this component. Also we say that a component is <b>indivisible</b> if
 * {@link #hasSubComponents()} returns false.
 * <p>
 * To make adjacent components non-intersecting, we use the convention that the area
 * this instance represents is spanned by the half-open intervals
 * [minX, maxX) x [minY, maxY). A point in then said to be contained in a component
 * if it is contained in [minX, maxX) x [minY, maxY) for that component.
 */
public class Component implements Id {

    public static final Logger logger = Logger.getLogger(Component.class.getName());
    public static final int
            AVAILABLE_ARRAYLISTS_OF_COMPONENT = 200,
            MAXIMAL_COMPONENT_SIZE = 512,
            AVAILABLE_COMPONENT_HASHSETS = 10_000,
            AVAILABLE_ENTITY_HASHSETS = 10_000;
    private final static Function<Integer, Integer>
            COMPONENT_INDEX_TO_X_MAPPING = integer -> integer % 2,
            COMPONENT_INDEX_TO_Y_MAPPING = integer -> {
                if (integer == 0 || integer == 1) return 0;
                if (integer == 2 || integer == 3) return 1;
                return null; // This should crash.
            };

    private static final IndexedSetPool<Component> COMPONENT_INDEXED_SET_POOL = new IndexedSetPool<>(AVAILABLE_COMPONENT_HASHSETS, i -> new IndexedSet<>());
    private static final IndexedSetPool<Entity> ENTITY_INDEXED_SET_POOL = new IndexedSetPool<>(AVAILABLE_ENTITY_HASHSETS, i -> new IndexedSet<>());
    // Keep an object pool for ArrayList<Component> instances.
    private static final ComponentArrayListPool SEARCH_ALG_OBJECT_POOL = new ComponentArrayListPool(
            AVAILABLE_ARRAYLISTS_OF_COMPONENT, i -> new ArrayList<>(), List::clear
    );

    private static int instancesInitiated = 0;
    private static long timeStampLastEntityWhereaboutsRegistered = Long.MIN_VALUE;
    private static List<Entity> entitiesInCameraNeighborhoodCached;
    private static long entitiesInCameraNeighborhoodLastUpdated = Long.MIN_VALUE;
    private static List<Entity> entitiesInCameraBoundsCached;
    private static long entitiesInCameraBoundsLastUpdated = Long.MIN_VALUE;
    private static Map<Entity, IndexedSet<Component>> entityToIndivisibleComponentMapping = new HashMap<>();
    private static Map<Component, IndexedSet<Entity>> indivisibleComponentToEntityMapping = new HashMap<>();
    private final int depth; // The level above root.
    private final ComponentParameters parameters;
    private final Component[] subComponents;
    private final EnumMap<Data, Object> data = new EnumMap<>(Data.class);
    private final int minX;
    private final int minY;
    private final RectInt underlyingRectInt;
    private final RectFloat underlyingRectFloat;
    private final float scaleFactor;
    // Functions of primitives.
    private final EnumMap<Data, IntBinaryOperator> intDataMapperByKey = new EnumMap<>(Data.class);
    private final EnumMap<Data, BiIntToFloatFunction> doubleDataMapperByKey = new EnumMap<>(Data.class);
    private final int id;
    private Status status = Status.DEACTIVATED;

    Component(@NotNull ComponentParameters parameters) {

        id = instancesInitiated++;

        // Early exit.
        if (!parameters.hasSubcomponents()) {
            throw new IllegalArgumentException("Received invalid parameters: " + parameters.toString());
        }

        underlyingRectInt = new RectInt(parameters.getX(), parameters.getY(), parameters.getWidth() - 1,
                parameters.getHeight() - 1);
        scaleFactor = parameters.getScaleFactor();
        underlyingRectFloat = new RectFloat(
                parameters.getX() * scaleFactor,
                parameters.getY() * scaleFactor,
                parameters.getWidth() * scaleFactor,
                parameters.getHeight() * scaleFactor
        );
        minX = parameters.getX();
        minY = parameters.getY();

        this.parameters = parameters;

        final List<ComponentParameters> componentParametersList = IntStream.range(0, 4).mapToObj(
                integer -> new ComponentParameters()
                        .setX(parameters.getX() + COMPONENT_INDEX_TO_X_MAPPING.apply(integer) * parameters.getWidth() / 2)
                        .setY(parameters.getY() + COMPONENT_INDEX_TO_Y_MAPPING.apply(integer) * parameters.getHeight() / 2)
                        .setWidth(parameters.getWidth() / 2)
                        .setHeight(parameters.getHeight() / 2)
                        .setScaleFactor(scaleFactor)
        ).collect(Collectors.toList());

        // One has subcomponents <=> all have subcomponents.
        if (componentParametersList.get(0).hasSubcomponents()) {

            subComponents = componentParametersList.stream()
                    .map(Component::new)
                    .toArray(Component[]::new);

            final int midPointX = minX + parameters.getWidth() / 2;
            final int midPointY = minY + parameters.getHeight() / 2;

            // Glue functions together on boundaries:
            for (Data key : Data.values()) {
                switch (key.getType()) {
                    case FLOAT -> doubleDataMapperByKey.put(key, (x, y) -> {
                        if (!underlyingRectContains(x, y)) return 0;

                        // See definition of adjacency in javadoc of this class.
                        if (x < midPointX) {
                            if (y < midPointY) {
                                // Upper left square
                                return subComponents[0].getDataFloat(x, y, key);
                            } else {
                                // Lower left square
                                return subComponents[2].getDataFloat(x, y, key);
                            }
                        } else {
                            if (y < midPointY) {
                                // Upper right square
                                return subComponents[1].getDataFloat(x, y, key);
                            } else {
                                // Lower right square
                                return subComponents[3].getDataFloat(x, y, key);
                            }
                        }
                    });
                    case INTEGER -> intDataMapperByKey.put(key, (x, y) -> {
                        if (!underlyingRectContains(x, y)) return 0;

                        // See definition of adjacency in javadoc of this class.
                        if (x < midPointX) {
                            if (y < midPointY) {
                                // Upper left square
                                return subComponents[0].getDataInt(x, y, key);
                            } else {
                                // Lower left square
                                return subComponents[2].getDataInt(x, y, key);
                            }
                        } else {
                            if (y < midPointY) {
                                // Upper right square
                                return subComponents[1].getDataInt(x, y, key);
                            } else {
                                // Lower right square
                                return subComponents[3].getDataInt(x, y, key);
                            }
                        }
                    });
                }
            }
        } else {
            subComponents = null;
            for (Data key : Data.values()) {
                switch (key.getType()) {
                    case INTEGER -> intDataMapperByKey.put(key, (x, y) -> {
                        if (underlyingRectInt.contains(x, y)) {
                            final int i = x - minX, j = y - minY;
                            final IntMatrix dataForKey = (IntMatrix) data.get(key);
                            return (dataForKey != null) ? dataForKey.get(i, j) : 0;
                        } else return 0;
                    });
                    case FLOAT -> doubleDataMapperByKey.put(key, (x, y) -> {
                        if (underlyingRectFloat.contains(x, y)) {
                            final int i = x - minX, j = y - minY;
                            final FloatMatrix dataForKey = (FloatMatrix) data.get(key);
                            return (dataForKey != null) ? dataForKey.get(i, j) : 0.0f;
                        } else return 0.0f;
                    });
                }
            }
        }

        // Figure out the depth of this component.
        int depth = 0;
        Component componentStack = this;

        while (componentStack.subComponents != null) {
            componentStack = componentStack.subComponents[0];
            depth++;
        }
        this.depth = depth;
    }

    public static void updateLocation(@NotNull Entity entity, @NotNull Component component) {
        updateLocation(Component.entityToIndivisibleComponentMapping, Component.indivisibleComponentToEntityMapping,
                entity, entity.graphicsNbd(), component);
    }

    @NotNull
    @Contract("_, _, _ -> new")
    public static Component representationOf(int width, int height, float scaleFactor) {
        int x = MAXIMAL_COMPONENT_SIZE, y = MAXIMAL_COMPONENT_SIZE;
        while (x < width * 2 || y < height * 2) {
            x *= 2;
            y *= 2;
        }
        logger.log(Level.WARNING, "Creating components with dimensions {" + x + ", " + y + "}");
        return new Component(new ComponentParameters().setX(-x / 2).setY(-y / 2).setWidth(x).setHeight(y).setScaleFactor(scaleFactor));
    }

    /**
     * If the neighborhood of the given entity intersects this component,
     * then the entity will be added to this components entity list. Otherwise, it is removed.
     * A recursive call is made to all of this components sub-components method.
     *
     * @param entity the entity whose whereabouts are stored.
     */
    private static void updateLocation(
            Map<Entity, IndexedSet<Component>> entityToIndivisibleComponentMapping,
            Map<Component, IndexedSet<Entity>> indivisibleComponentToEntityMapping,
            @NotNull Entity entity,
            /* Caching the below calculation makes a big difference.*/
            @NotNull RectInt cachedEntityNodeNeighborhood,
            @NotNull Component component
    ) {
        if (component.underlyingRectInt.meets(cachedEntityNodeNeighborhood)) {
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
     * Registers the whereabouts of all entities from the given list.
     * This updates the information returned by {@link #getContainedEntities(RectInt)}.
     *
     * @param component on which component to register the data to.
     */
    public static void registerEntityWhereabouts(@NotNull Component component) {


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

        EntityCollectionProvider.actionForEachEntityOrderedByGroup(e -> updateLocation(entityToIndivisibleComponentMapping,
                indivisibleComponentToEntityMapping,
                e,
                e.graphicsNbd(),
                component
        ));

        // Update static references; keep the data ready to be cleared around until the beginning of the next iteration.
        Component.entityToIndivisibleComponentMapping = entityToIndivisibleComponentMapping;
        Component.indivisibleComponentToEntityMapping = indivisibleComponentToEntityMapping;
        timeStampLastEntityWhereaboutsRegistered = System.currentTimeMillis();
    }

    public static List<Entity> getEntityInCameraNeighborhoodCached() {
        if (entitiesInCameraNeighborhoodLastUpdated <= timeStampLastEntityWhereaboutsRegistered) {
            entitiesInCameraNeighborhoodLastUpdated = System.currentTimeMillis();
            return entitiesInCameraNeighborhoodCached = GlobalData.rootComponent
                    .getContainedEntities(GlobalData.currentCameraBounds);
        }
        return entitiesInCameraNeighborhoodCached;
    }

    public static List<Entity> getEntitiesWithinCameraBoundsCached() {
        if (entitiesInCameraBoundsLastUpdated <= timeStampLastEntityWhereaboutsRegistered) {
            entitiesInCameraBoundsLastUpdated = System.currentTimeMillis();
            return entitiesInCameraBoundsCached = GlobalData.rootComponent
                    .getContainedEntities(GlobalData.currentCameraBounds);
        }
        return entitiesInCameraBoundsCached;
    }

    @Borrowed(origin = "SEARCH_ALG_OBJECT_POOL")
    public static @NotNull ArrayList<Component> treeSearchFindIndivisibleComponentsMatching(
            Component root,
            Predicate<Component> condition
    ) {
        ArrayList<Component> matches = SEARCH_ALG_OBJECT_POOL.borrow();
        ArrayList<Component> layer = SEARCH_ALG_OBJECT_POOL.borrow();
        layer.add(root);
        treeSearchRecursionStep(matches, layer, condition);
        SEARCH_ALG_OBJECT_POOL.giveBackAllExcept(matches);
        if (SEARCH_ALG_OBJECT_POOL.taken() != 1)
            throw new RuntimeException("No of taken: " + SEARCH_ALG_OBJECT_POOL.taken());
        return matches;
    }

    private static void treeSearchRecursionStep(
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
                for (Component child : layer.get(i).subComponents) {
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


    public static void clearUnusedData(Component component, @NotNull RectInt rect) {
        final ArrayList<Component> treeSearchResult = treeSearchFindIndivisibleComponentsMatching(
                component,
                c -> c.underlyingRectInt.meets(rect)
        );
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0, n = treeSearchResult.size(); i < n; i++) {
            final Component c = treeSearchResult.get(i);
            if (c.getStatus() == Status.DEACTIVATED && indivisibleComponentToEntityMapping.containsKey(c)) {
                c.clearData();
                c.setStatus(Status.DEACTIVATED);
            }
        }
        SEARCH_ALG_OBJECT_POOL.giveBack(treeSearchResult);
    }

    public static void loadNearbyData(Component component, @NotNull RectInt rect) {
        final ArrayList<Component> treeSearchResult = treeSearchFindIndivisibleComponentsMatching(
                component,
                c -> c.underlyingRectInt.meets(rect)
        );
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0, n = treeSearchResult.size(); i < n; i++) {
            final Component c = treeSearchResult.get(i);
            if (c.getStatus() == Status.DEACTIVATED && indivisibleComponentToEntityMapping.containsKey(c)) {

                final IndexedSet<Entity> entities = indivisibleComponentToEntityMapping.get(c);
                for (int j = 0, m = entities.size(); j < m; j++) {
                    Entity entity = entities.get(j);
                    if (entity instanceof CollisionData) {
                        ((CollisionData) entity).actionEachCollisionPoint(component::incrementCollisionAt);
                    }
//                    if (entity instanceof LadderData) {
//                        ((LadderData) entity).initLadderData(c);
//                    }
                }
                c.setStatus(Status.ACTIVE);
            }
        }
        SEARCH_ALG_OBJECT_POOL.giveBack(treeSearchResult);
    }

    public List<Component> getSubComponents() {
        return List.of(subComponents);
    }

    public boolean hasSubComponents() {
        return subComponents != null;
    }

    public int getPositionX() {
        return parameters.getX();
    }

    public int getPositionY() {
        return parameters.getY();
    }

    public int getWidth() {
        return parameters.getWidth();
    }

    public int getHeight() {
        return parameters.getHeight();
    }

    public RectFloat getUnderlyingRectFloat() {
        return underlyingRectFloat;
    }

    /**
     * Returns true if [minX, maxX) and [minY, maxY) contains the point (x,y) evaluated.
     */
    public boolean underlyingRectContains(int x, int y) {
        return underlyingRectInt.contains(x, y);
    }

    public float getDataFloat(int x, int y, @NotNull Component.Data key) {
        return doubleDataMapperByKey.get(key).eval(x, y);
    }

    public int getDataInt(int x, int y, @NotNull Component.Data key) {
        return intDataMapperByKey.get(key).eval(x, y);
    }


    /**
     * Finds list of entities currently occupying the screen.
     */
    @NotNull
    public List<Entity> getContainedEntities(@NotNull RectInt neighborhood) {
        ArrayList<Component> treeSearchResult = treeSearchFindIndivisibleComponentsMatching(
                GlobalData.rootComponent,
                c -> c.underlyingRectInt.meets(neighborhood)
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

    @Override
    public int getId() {
        return id;
    }

    public int getCollision(int x, int y) {
        return intDataMapperByKey.get(Data.COLLISION).eval(x, y);
    }

    public int getLadder(int x, int y) {
        return intDataMapperByKey.get(Data.LADDER).eval(x, y);
    }

    public float getVectorFieldX(int x, int y) {
        return doubleDataMapperByKey.get(Data.VECTOR_FIELD_X).eval(x, y);
    }

    public float getVectorFieldY(int x, int y) {
        return doubleDataMapperByKey.get(Data.VECTOR_FIELD_Y).eval(x, y);
    }

    public void modifyVectorFieldAt(int x, int y, FloatUnaryOperation transformX, FloatUnaryOperation transformY) {
        if (underlyingRectContains(x, y)) {
            if (hasSubComponents()) {
                for (Component c : subComponents) {
                    c.modifyVectorFieldAt(x, y, transformX, transformY);
                }
            } else {
                if (!data.containsKey(Data.VECTOR_FIELD_X)) {
                    data.put(Data.VECTOR_FIELD_X, FloatArrayPool.getInstance().borrow());
                }
                if (!data.containsKey(Data.VECTOR_FIELD_Y)) {
                    data.put(Data.VECTOR_FIELD_Y, FloatArrayPool.getInstance().borrow());
                }
                final int
                        i = x - minX,
                        j = y - minY;
                ((FloatMatrix) data.get(Data.VECTOR_FIELD_X)).modify(i, j, transformX);
                ((FloatMatrix) data.get(Data.VECTOR_FIELD_Y)).modify(i, j, transformY);
            }
        }
    }

    public void increment(int x, int y, Data key) {
        if (underlyingRectContains(x, y)) {
            if (hasSubComponents()) {
                for (Component c : subComponents) {
                    c.increment(x, y, key);
                }
            } else {
                if (!data.containsKey(key)) {
                    data.put(key, IntMatrixPool.getInstance().borrow());
                }
                ((IntMatrix) data.get(key)).increment(x - minX, y - minY);

                // Debugging
                {
                    //System.out.println("Incremented at: " + Point.of(x, y));
                    int val = ((IntMatrix) data.get(key)).get(x - minX, y - minY);
                    if (val > 1) {
                        System.out.println("A collision point was raised to :" + val);
                    }
                }
            }
        }
    }

    /**
     * Clears any data associated with any of this components sub-components, or if
     * this has no sub-component, then clears all data associated with this.
     */
    public void clearData() {
        if (hasSubComponents()) {
            for (Component c : subComponents) {
                c.clearData();
            }
        } else {
            // Data which has been initialized (taken from the pool), is returned.
            {
                Object o = data.get(Data.COLLISION);
                if (o != null) {
                    IntMatrixPool.getInstance().giveBack((IntMatrix) o);
                }
            }
            {
                Object o = data.get(Data.LADDER);
                if (o != null) {
                    IntMatrixPool.getInstance().giveBack((IntMatrix) o);
                }
            }
            {
                Object o = data.get(Data.VECTOR_FIELD_X);
                if (o != null) {
                    FloatArrayPool.getInstance().giveBack((FloatMatrix) o);
                }
            }
            {
                Object o = data.get(Data.VECTOR_FIELD_Y);
                if (o != null) {
                    FloatArrayPool.getInstance().giveBack((FloatMatrix) o);
                }
            }
            data.clear();
        }
    }

    @Override
    public String toString() {
        return "{x: " + underlyingRectInt.getMinX() / COARSENESS_PARAMETER +
                ", y: " + underlyingRectInt.getMinY() / COARSENESS_PARAMETER + "}";
    }

    public void decrement(int x, int y, Data key) {
        if (underlyingRectContains(x, y)) {
            if (hasSubComponents()) {
                for (Component c : subComponents) {
                    c.decrement(x, y, key);
                }
            } else {
                if (data.containsKey(key)) {
                    final IntMatrix dataUnderKey = (IntMatrix) data.get(key);
                    if (dataUnderKey != null) {
                        final int
                                i = x - minX,
                                j = y - minY;
                        dataUnderKey.decrement(i, j);
                    }
                } else
                    throw new RuntimeException(
                            String.format("The data of type '%s' at position (%s,%s) for this sub-component was empty. "
                                    + "This method should not have been called.", key.name(), x, y));
            }
        }
    }

    public void incrementCollisionAt(int x, int y) {
        increment(x, y, Data.COLLISION);
    }

    public void decrementCollisionAt(int x, int y) {
        decrement(x, y, Data.COLLISION);
    }

    public boolean isCollisionAt(int x, int y) {
        return getCollision(x, y) > 0;
    }

    public boolean isLadderAt(int x, int y) {
        return getLadder(x, y) > 0;
    }

    public boolean isCollisionIfNotLadderData(int x, int y) {
        if (isLadderAt(x, y)) return false;
        else return (isCollisionAt(x, y));
    }

    public boolean collisionForScaledCoordinatesAt(float x, float y) {
        return isCollisionAt(Functions.toIntDivideBy(x, scaleFactor), Functions.toIntDivideBy(y, scaleFactor));
    }

    public void increaseVectorFieldXAt(int x, int y) {
        increment(x, y, Data.VECTOR_FIELD_X);
    }

    public void decreaseVectorFieldXAt(int x, int y) {
        decrement(x, y, Data.VECTOR_FIELD_X);
    }

    public void increaseVectorFieldYAt(int x, int y) {
        increment(x, y, Data.VECTOR_FIELD_Y);
    }

    public void decreaseVectorFieldYAt(int x, int y) {
        decrement(x, y, Data.VECTOR_FIELD_Y);
    }

    public void increaseLadderAt(int x, int y) {
        increment(x, y, Data.LADDER);
    }

    public void decreaseLadderAt(int x, int y) {
        decrement(x, y, Data.LADDER);
    }

    public int getDepth() {
        return depth;
    }

    public Status getStatus() {
        return status;
    }

    private void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public final boolean equals(Object o) {
        return this == o;
    }

    @Override
    public final int hashCode() {
        return id;
    }

    /**
     * Helper enum to classify the types of each data that we encounter for the worlds.
     * As it stands, for each {@code int} pair,  we see;
     * <ul>
     *     <li>Collision data (represented by {@code int} values.)</li>
     *     <li>Ladder data (represented by {@code int} values.)</li>
     *     <li>Vector field X data (represented by {@code float} values.)</li>
     *     <li>Vector field Y data (represented by {@code float} values.)</li>
     * </ul>
     */
    public enum Data {
        COLLISION(PrimitiveType.INTEGER),
        LADDER(PrimitiveType.INTEGER),
        VECTOR_FIELD_X(PrimitiveType.FLOAT),
        VECTOR_FIELD_Y(PrimitiveType.FLOAT);

        private final PrimitiveType primitiveType;

        Data(PrimitiveType primitiveType) {
            this.primitiveType = primitiveType;
        }

        public PrimitiveType getType() {
            return primitiveType;
        }

        /**
         * To further help with classification, to each primitive types we encounter associate a constant.
         */
        enum PrimitiveType {
            INTEGER,
            FLOAT
        }

    }

    enum Status {LOADING, ACTIVE, DEACTIVATED}

    private static class ComponentArrayListPool extends AbstractObjectPool<ArrayList<Component>> {

        public ComponentArrayListPool(int objectsAvailable, IntFunction<ArrayList<Component>> mapper,
                                      Consumer<ArrayList<Component>> clearDataProcedure) {
            super(objectsAvailable, mapper, clearDataProcedure);
        }

    }

    static class IndexedSetPool<Entry extends Id> extends AbstractObjectPool<IndexedSet<Entry>> {
        public IndexedSetPool(int objectsAvailable, IntFunction<IndexedSet<Entry>> mapper) {
            super(objectsAvailable, mapper, IndexedSet::clear);
        }
    }

    /**
     * Makes sure that int data objects are re-used, instead of being garbage collected.
     */
    static class IntMatrixPool extends AbstractObjectPool<IntMatrix> {

        final static int OBJECTS_AVAILABLE = 64 * 2; // 2 because Ladder data & Collision data.

        private static final IntMatrixPool instance
                = new IntMatrixPool(OBJECTS_AVAILABLE, i -> new IntMatrix(COARSENESS_PARAMETER, COARSENESS_PARAMETER));

        IntMatrixPool(int objectsAvailable, IntFunction<IntMatrix> mapper) {
            super(objectsAvailable, mapper, IntMatrix::clear);
        }

        public static IntMatrixPool getInstance() {
            return instance;
        }
    }

    /**
     * Makes sure that float data objects are re-used, instead of being garbage collected.
     */
    static class FloatArrayPool extends AbstractObjectPool<FloatMatrix> {

        final static int OBJECTS_AVAILABLE = 64 * 2;  // 2 because vector field X & Y.

        private static final FloatArrayPool instance
                = new FloatArrayPool(OBJECTS_AVAILABLE, i -> new FloatMatrix(COARSENESS_PARAMETER, COARSENESS_PARAMETER));

        FloatArrayPool(int objectsAvailable, IntFunction<FloatMatrix> mapper) {
            super(objectsAvailable, mapper, m -> Arrays.fill(m.data(), 0));
        }

        public static FloatArrayPool getInstance() {
            return instance;
        }
    }
}
