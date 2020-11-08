package dev.kabin.components;


import dev.kabin.collections.Id;
import dev.kabin.collections.IndexedSet;
import dev.kabin.entities.CollisionData;
import dev.kabin.entities.Entity;
import dev.kabin.entities.EntityGroupProvider;
import dev.kabin.geometry.shapes.RectFloat;
import dev.kabin.geometry.shapes.RectInt;
import dev.kabin.global.GlobalData;
import dev.kabin.utilities.Functions;
import dev.kabin.utilities.functioninterfaces.IntPrimitiveBiFunctionToDouble;
import dev.kabin.utilities.functioninterfaces.IntPrimitiveBinaryOperator;
import dev.kabin.utilities.pools.objectpool.AbstractObjectPool;
import dev.kabin.utilities.pools.objectpool.OutputFromPool;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
    final static int MAXIMAL_COMPONENT_SIZE = 512;
    final static int
            AVAILABLE_COMPONENT_HASHSETS = 10_000,
            AVAILABLE_ENTITY_HASHSETS = 10_000;
    private final static Function<Integer, Integer> COMPONENT_INDEX_TO_X_MAPPING = integer -> integer % 2;
    private final static Function<Integer, Integer> COMPONENT_INDEX_TO_Y_MAPPING = integer -> {
        if (integer == 0 || integer == 1) return 0;
        if (integer == 2 || integer == 3) return 1;
        return null; // This should crash.
    };
    private static final IndexedSetPool<Component> COMPONENT_INDEXED_SET_POOL = new IndexedSetPool<>(AVAILABLE_COMPONENT_HASHSETS, i -> new IndexedSet<>());
    private static final IndexedSetPool<Entity> ENTITY_INDEXED_SET_POOL = new IndexedSetPool<>(AVAILABLE_ENTITY_HASHSETS, i -> new IndexedSet<>());
    // Keep an object pool for ArrayList<Component> instances.
    private static final ComponentArrayListPool SEARCH_ALG_OBJECT_POOL = new ComponentArrayListPool(
            200, i -> new ArrayList<>(), List::clear
    );
    private static int instancesInitiated = 0;
    private static long timeStampLastEntityWhereaboutsRegistered = Long.MIN_VALUE;
    private static List<Entity> entitiesInCameraNeighborhoodCached;
    private static long entitiesInCameraNeighborhoodLastUpdated = Long.MIN_VALUE;
    private static List<Entity> entitiesInCameraBoundsCached;
    private static long entitiesInCameraBoundsLastUpdated = Long.MIN_VALUE;
    private static Map<Entity, IndexedSet<Component>> entityToIndivisibleComponentMapping = new HashMap<>();
    private static Map<Component, IndexedSet<Entity>> indivisibleComponentToEntityMapping = new HashMap<>();
    final int depth; // The level above root.
    private final ComponentParameters parameters;
    private final Component[] subComponents;
    private final EnumMap<DataType, Object> data = new EnumMap<>(DataType.class);
    private final int minX;
    private final int minY;
    private final RectInt underlyingRectInt;
    private final RectFloat underlyingRectFloat;
    private final float scaleFactor;
    // Functions of primitives.
    private final EnumMap<DataType, IntPrimitiveBinaryOperator> intDataMapperByKey = new EnumMap<>(DataType.class);
    private final EnumMap<DataType, IntPrimitiveBiFunctionToDouble> doubleDataMapperByKey = new EnumMap<>(DataType.class);
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

        final Stream<ComponentParameters> componentParameterStream = IntStream.range(0, 4).mapToObj(
                integer -> new ComponentParameters()
                        .setX(parameters.getX() + COMPONENT_INDEX_TO_X_MAPPING.apply(integer) * parameters.getWidth() / 2)
                        .setY(parameters.getY() + COMPONENT_INDEX_TO_Y_MAPPING.apply(integer) * parameters.getHeight() / 2)
                        .setWidth(parameters.getWidth() / 2)
                        .setHeight(parameters.getHeight() / 2)
                        .setScaleFactor(scaleFactor)
        );
        final List<ComponentParameters> componentParametersList = componentParameterStream.collect(Collectors.toList());

        // One has subcomponents <=> all have subcomponents.
        if (componentParametersList.get(0).hasSubcomponents()) {

            subComponents = componentParametersList.stream()
                    .map(Component::new)
                    .toArray(Component[]::new);

            final int midPointX = minX + parameters.getWidth() / 2;
            final int midPointY = minY + parameters.getHeight() / 2;

            for (DataType key : DataType.values()) {

                // Glue together int-functions.
                if (key.getType() == int[][].class) {
                    intDataMapperByKey.put(key, (x, y) -> {

                        if (!underlyingRectContains(x, y)) return 0;

                        // See defn. of adjacency in javadoc of this class.
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

                // Glue together double-functions.
                if (key.getType() == double[][].class) {
                    doubleDataMapperByKey.put(key, (x, y) -> {
                        if (!underlyingRectContains(x, y)) return 0;

                        // See defn. of adjacency in javadoc of this class.
                        if (x < midPointX) {
                            if (y < midPointY) {
                                // Upper left square
                                return subComponents[0].getDataDouble(x, y, key);
                            } else {
                                // Lower left square
                                return subComponents[2].getDataDouble(x, y, key);
                            }
                        } else {
                            if (y < midPointY) {
                                // Upper right square
                                return subComponents[1].getDataDouble(x, y, key);
                            } else {
                                // Lower right square
                                return subComponents[3].getDataDouble(x, y, key);
                            }
                        }
                    });
                }

            }
        } else {
            subComponents = null;
            for (DataType key : DataType.values()) {
                if (key.getType().equals(int[][].class)) {
                    intDataMapperByKey.put(key, (x, y) -> {
                        if (underlyingRectInt.contains(x, y)) {
                            final int i = x - minX, j = y - minY;
                            final int[][] dataForKey = (int[][]) data.get(key);
                            return (dataForKey != null) ? dataForKey[i][j] : 0;
                        } else return 0;
                    });
                } else if (key.getType().equals(double[][].class)) {
                    doubleDataMapperByKey.put(key, (x, y) -> {
                        if (underlyingRectFloat.contains(x, y)) {
                            final int i = x - minX, j = y - minY;
                            final double[][] dataForKey = (double[][]) data.get(key);
                            return (dataForKey != null) ? dataForKey[i][j] : 0.0d;
                        } else return 0.0d;
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

    public static void clearEntityMapping() {
        entityToIndivisibleComponentMapping.clear();
    }

    public static void updateLocation(@NotNull Entity entity, @NotNull Component component) {
        updateLocation(Component.entityToIndivisibleComponentMapping, Component.indivisibleComponentToEntityMapping,
                entity, entity.graphicsNbd(), component);
    }

    @NotNull
    @Contract("_, _ -> new")
    public static Component getComponentRepresentation(int width, int height) {
        int x = MAXIMAL_COMPONENT_SIZE, y = MAXIMAL_COMPONENT_SIZE;
        while (x < width * 2 || y < height * 2) {
            x *= 2;
            y *= 2;
        }
        logger.log(Level.WARNING, "Creating components with dimensions {" + x + " ," + y + "}");
        return new Component(new ComponentParameters().setWidth(x).setHeight(y).setScaleFactor(GlobalData.scaleFactor));
    }

    /**
     * If the image view neighborhood of the given entity intersects this component,
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

    	Freeing resources right before the next iteration as opposed to at the end of the previous iteration,
    	is in order to let potential unfinished JavaFX threads finish with their List<Entity> instances
    	without the data being cleared while they are busy.
    	 */
        {

            // Give back data to pools.
            COMPONENT_INDEXED_SET_POOL.giveBackAll();
            if (COMPONENT_INDEXED_SET_POOL.taken() > 0)
                throw new RuntimeException("There were so many: " + COMPONENT_INDEXED_SET_POOL.taken());

            ENTITY_INDEXED_SET_POOL.giveBackAll();
            if (ENTITY_INDEXED_SET_POOL.taken() > 0) throw new RuntimeException();
        }


        final Map<Entity, IndexedSet<Component>> entityToIndivisibleComponentMapping = new HashMap<>();
        final Map<Component, IndexedSet<Entity>> indivisibleComponentToEntityMapping = new HashMap<>();

        EntityGroupProvider.actionForEachEntityOrderedByGroup(e -> updateLocation(entityToIndivisibleComponentMapping,
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

    @OutputFromPool(pool = "SEARCH_ALG_OBJECT_POOL")
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


    public static void clearUnusedData(@NotNull RectInt rect) {
        final ArrayList<Component> treeSearchResult = treeSearchFindIndivisibleComponentsMatching(
                GlobalData.rootComponent,
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

    public static void loadNearbyData(@NotNull RectInt rect) {
        final ArrayList<Component> treeSearchResult = treeSearchFindIndivisibleComponentsMatching(
                GlobalData.rootComponent,
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
                        ((CollisionData) entity).initCollisionData(c);
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

    public double getDataDouble(int x, int y, @NotNull DataType key) {
        return doubleDataMapperByKey.get(key).apply(x, y);
    }

    public int getDataInt(int x, int y, @NotNull DataType key) {
        return intDataMapperByKey.get(key).apply(x, y);
    }


    /**
     * Finds list of entities currently occupying the screen.
     */
    @NotNull
    private List<Entity> getContainedEntities(@NotNull RectInt neighborhood) {
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
        return intDataMapperByKey.get(DataType.COLLISION).apply(x, y);
    }

    public int getLadder(int x, int y) {
        return intDataMapperByKey.get(DataType.LADDER).apply(x, y);
    }

    public double getVectorFieldX(int x, int y) {
        return doubleDataMapperByKey.get(DataType.VECTOR_FIELD_X).apply(x, y);
    }

    public double getVectorFieldY(int x, int y) {
        return doubleDataMapperByKey.get(DataType.VECTOR_FIELD_Y).apply(x, y);
    }

    public void modifyVectorFieldAt(int x, int y, UnaryOperator<Double> transformX, UnaryOperator<Double> transformY) {
        if (underlyingRectContains(x, y)) {
            if (hasSubComponents()) {
                for (Component c : subComponents) {
                    c.modifyVectorFieldAt(x, y, transformX, transformY);
                }
            } else {
                final int
                        i = x - minX,
                        j = y - minY;

                if (!data.containsKey(DataType.VECTOR_FIELD_X)) {
                    data.put(DataType.VECTOR_FIELD_X, DoubleArrayPool.getInstance().borrow());
                }
                if (!data.containsKey(DataType.VECTOR_FIELD_Y)) {
                    data.put(DataType.VECTOR_FIELD_Y, DoubleArrayPool.getInstance().borrow());
                }

                final double[][] vectorFieldX = (double[][]) data.get(DataType.VECTOR_FIELD_X);
                vectorFieldX[i][j] = transformX.apply(vectorFieldX[i][j]);

                final double[][] vectorFieldY = (double[][]) data.get(DataType.VECTOR_FIELD_Y);
                vectorFieldY[i][j] = transformY.apply(vectorFieldY[i][j]);
            }
        }
    }

    public void increaseAt(int x, int y, DataType key) {
        if (underlyingRectContains(x, y)) {
            if (hasSubComponents()) {
                for (Component c : subComponents) {
                    c.increaseAt(x, y, key);
                }
            } else {
                final int
                        i = x - minX,
                        j = y - minY;

                final boolean integerKeyType = key.getType().equals(int[][].class);
                final boolean doubleKeyType = key.getType().equals(double[][].class);

                if (!data.containsKey(key)) {
                    if (integerKeyType) {
                        data.put(key, IntArrayPool.getInstance().borrow());
                    } else if (doubleKeyType) {
                        data.put(key, DoubleArrayPool.getInstance().borrow());
                    }
                }

                if (integerKeyType) {
                    final int[][] dataUnderKey = (int[][]) data.get(key);
                    dataUnderKey[i][j] = dataUnderKey[i][j] + 1;
                } else if (doubleKeyType) {
                    final double[][] dataUnderKey = (double[][]) data.get(key);
                    dataUnderKey[i][j] = dataUnderKey[i][j] + 1;
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
                Object o = data.get(DataType.COLLISION);
                if (o != null) {
                    IntArrayPool.getInstance().giveBack((int[][]) o);
                }
            }
            {
                Object o = data.get(DataType.LADDER);
                if (o != null) {
                    IntArrayPool.getInstance().giveBack((int[][]) o);
                }
            }
            {
                Object o = data.get(DataType.VECTOR_FIELD_X);
                if (o != null) {
                    DoubleArrayPool.getInstance().giveBack((double[][]) o);
                }
            }
            {
                Object o = data.get(DataType.VECTOR_FIELD_Y);
                if (o != null) {
                    DoubleArrayPool.getInstance().giveBack((double[][]) o);
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

    public void decreaseAt(int x, int y, DataType key) {
        if (underlyingRectContains(x, y)) {
            if (hasSubComponents()) {
                for (Component c : subComponents) {
                    c.decreaseAt(x, y, key);
                }
            } else {
                if (data.containsKey(key)) {
                    final int
                            i = x - minX,
                            j = y - minY;

                    if (key.getType().equals(int[][].class)) {
                        final int[][] dataUnderKey = (int[][]) data.get(key);
                        if (dataUnderKey != null) {
                            dataUnderKey[i][j] = dataUnderKey[i][j] - 1;
                        }
                    } else if (key.getType().equals(double[][].class)) {
                        final double[][] dataUnderKey = (double[][]) data.get(key);
                        if (dataUnderKey != null) {
                            dataUnderKey[i][j] = dataUnderKey[i][j] - 1;
                        }
                    }
                } else
                    throw new RuntimeException(
                            String.format("The data of type '%s' at position (%s,%s) for this sub-component was empty. "
                                    + "This method should not have been called.", key.name(), x, y));
            }
        }
    }

    public void increaseCollisionAt(int x, int y) {
        increaseAt(x, y, DataType.COLLISION);
    }

    public void decreaseCollisionAt(int x, int y) {
        decreaseAt(x, y, DataType.COLLISION);
    }

    public boolean collisionAt(int x, int y) {
        return getCollision(x, y) > 0;
    }

    public boolean ladderAt(int x, int y) {
        return getLadder(x, y) > 0;
    }

    public boolean collisionIfNotLadderData(int x, int y) {
        if (ladderAt(x, y)) return false;
        else return (collisionAt(x, y));
    }

    public boolean collisionForScaledCoordinatesAt(double x, double y) {
        return collisionAt(Functions.toInt(x, scaleFactor), Functions.toInt(y, scaleFactor));
    }

    public void increaseVectorFieldXAt(int x, int y) {
        increaseAt(x, y, DataType.VECTOR_FIELD_X);
    }

    public void decreaseVectorFieldXAt(int x, int y) {
        decreaseAt(x, y, DataType.VECTOR_FIELD_X);
    }

    public void increaseVectorFieldYAt(int x, int y) {
        increaseAt(x, y, DataType.VECTOR_FIELD_Y);
    }

    public void decreaseVectorFieldYAt(int x, int y) {
        decreaseAt(x, y, DataType.VECTOR_FIELD_Y);
    }

    public void increaseLadderAt(int x, int y) {
        increaseAt(x, y, DataType.LADDER);
    }

    public void decreaseLadderAt(int x, int y) {
        decreaseAt(x, y, DataType.LADDER);
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

    public enum DataType {
        COLLISION(int[][].class),
        LADDER(int[][].class),
        VECTOR_FIELD_X(double[][].class),
        VECTOR_FIELD_Y(double[][].class);

        private final Class<?> type;

        DataType(Class<?> type) {
            this.type = type;
        }

        public Class<?> getType() {
            return type;
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
    static class IntArrayPool extends AbstractObjectPool<int[][]> {

        final static int OBJECTS_AVAILABLE = 64 * 2; // 2 because Ladder data & Collision data.

        private static final IntArrayPool instance
                = new IntArrayPool(OBJECTS_AVAILABLE, i -> new int[COARSENESS_PARAMETER][COARSENESS_PARAMETER]);

        IntArrayPool(int objectsAvailable, IntFunction<int[][]> mapper) {
            super(objectsAvailable, mapper, doubleIntArray -> {
                //noinspection ForLoopReplaceableByForEach
                for (int i = 0, n = doubleIntArray.length; i < n; i++) {
                    Arrays.fill(doubleIntArray[i], 0);
                }
            });
        }

        public static IntArrayPool getInstance() {
            return instance;
        }
    }

    /**
     * Makes sure that double data objects are re-used, instead of being garbage collected.
     */
    static class DoubleArrayPool extends AbstractObjectPool<double[][]> {

        final static int OBJECTS_AVAILABLE = 64 * 2;  // 2 because vector field X & Y.

        private static final DoubleArrayPool instance
                = new DoubleArrayPool(OBJECTS_AVAILABLE, i -> new double[COARSENESS_PARAMETER][COARSENESS_PARAMETER]);

        DoubleArrayPool(int objectsAvailable, IntFunction<double[][]> mapper) {
            super(objectsAvailable, mapper, doubleArray2D -> {
                //noinspection ForLoopReplaceableByForEach
                for (int i = 0, n = doubleArray2D.length; i < n; i++) {
                    Arrays.fill(doubleArray2D[i], 0);
                }
            });
        }

        public static DoubleArrayPool getInstance() {
            return instance;
        }
    }
}
