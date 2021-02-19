package dev.kabin.components;


import dev.kabin.collections.Id;
import dev.kabin.components.worldmodel.FloatArrayPool;
import dev.kabin.components.worldmodel.IntMatrixPool;
import dev.kabin.util.Functions;
import dev.kabin.util.functioninterfaces.BiIntToFloatFunction;
import dev.kabin.util.functioninterfaces.FloatUnaryOperation;
import dev.kabin.util.functioninterfaces.IntBinaryOperator;
import dev.kabin.util.linalg.FloatMatrix;
import dev.kabin.util.linalg.IntMatrix;
import dev.kabin.util.shapes.RectFloat;
import dev.kabin.util.shapes.primitive.ImmutableRectInt;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


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

    private static int instancesInitiated = 0;

    private final static Function<Integer, Integer>
            COMPONENT_INDEX_TO_X_MAPPING = integer -> integer % 2,
            COMPONENT_INDEX_TO_Y_MAPPING = integer -> {
                if (integer == 0 || integer == 1) return 0;
                if (integer == 2 || integer == 3) return 1;
                return null; // This should crash.
            };

    private final int depth; // The level above root.
    private final ComponentParameters parameters;
    private final Component[] subComponents;
    private final EnumMap<Data, Object> data = new EnumMap<>(Data.class);
    private final int minX;
    private final int minY;
    private final ImmutableRectInt underlyingRectInt;
    private final RectFloat underlyingRectFloat;
    private final float scaleFactor;
    // Functions of primitives.
    private final EnumMap<Data, IntBinaryOperator> intDataMapperByKey = new EnumMap<>(Data.class);
    private final EnumMap<Data, BiIntToFloatFunction> doubleDataMapperByKey = new EnumMap<>(Data.class);
    private final int id;
    private boolean active = false;

    static Component make(ComponentParameters parameters){
        return new Component(parameters);
    }

    private Component(@NotNull ComponentParameters parameters) {

        id = instancesInitiated++;

        // Early exit.
        if (!parameters.hasSubcomponents()) {
            throw new IllegalArgumentException("Received invalid parameters: " + parameters.toString());
        }

        underlyingRectInt = new ImmutableRectInt(parameters.getX(), parameters.getY(), parameters.getWidth() - 1,
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
                integer -> ComponentParameters
                        .make()
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
                        if (!contains(x, y)) return 0;

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
                        if (!contains(x, y)) return 0;

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


    public ImmutableRectInt getUnderlyingRectInt() {
        return underlyingRectInt;
    }

    /**
     * @return an unmodifiable view of sub-components.
     */
    public List<Component> getSubComponents() {
        return List.of(subComponents);
    }

    public void forEachMatching(Consumer<Component> action, Predicate<Component> condition) {
        if (condition.test(subComponents[0])) action.accept(subComponents[0]);
        if (condition.test(subComponents[1])) action.accept(subComponents[1]);
        if (condition.test(subComponents[2])) action.accept(subComponents[2]);
        if (condition.test(subComponents[3])) action.accept(subComponents[3]);
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
    public boolean contains(int x, int y) {
        return underlyingRectInt.contains(x, y);
    }

    public float getDataFloat(int x, int y, @NotNull Component.Data key) {
        return doubleDataMapperByKey.get(key).eval(x, y);
    }

    public int getDataInt(int x, int y, @NotNull Component.Data key) {
        return intDataMapperByKey.get(key).eval(x, y);
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
        if (contains(x, y)) {
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

    public void activate(int x, int y) {
        if (contains(x, y)) {
            if (hasSubComponents()) {
                for (Component c : subComponents) {
                    c.activate(x, y);
                }
            } else {
                if (isInactive()) {
                    setActive(true);
                }
            }
        }
    }

    public void increment(int x, int y, Data key) {
        if (contains(x, y)) {
            if (hasSubComponents()) {
                for (Component c : subComponents) {
                    c.increment(x, y, key);
                }
            } else {
                if (!data.containsKey(key)) {
                    data.put(key, IntMatrixPool.getInstance().borrow());
                }

                ((IntMatrix) data.get(key)).increment(x - minX, y - minY);
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
        return "{" +
                "id: " + id +
                ", x: " + underlyingRectInt.getMinX() +
                ", y: " + underlyingRectInt.getMinY() +  "}";
    }

    public void decrement(int x, int y, Data key) {
        if (contains(x, y)) {
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


    public boolean isActive(){
        return active;
    }

    public boolean isInactive(){
        return !active;
    }

    public void setActive(boolean b) {
        this.active = b;
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

}
