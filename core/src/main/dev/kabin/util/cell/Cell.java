package dev.kabin.util.cell;


import dev.kabin.components.worldmodel.FloatMatrixPool;
import dev.kabin.components.worldmodel.IntMatrixPool;
import dev.kabin.util.collections.Id;
import dev.kabin.util.lambdas.BiIntToFloatFunction;
import dev.kabin.util.lambdas.FloatUnaryOperation;
import dev.kabin.util.lambdas.IntBinaryOperator;
import dev.kabin.util.linalg.FloatMatrix;
import dev.kabin.util.linalg.IntMatrix;
import dev.kabin.util.shapes.primitive.ImmutableRectInt;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;


/**
 * Represents a quadratic cell which may consist of 4 quadratic sub-cells:
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
 * This instance will have sub-components iff the provided {@link CellParameters} have {@link CellParameters#hasSubCells()}
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
public final class Cell implements Id {

    // Statics:
    private static final Logger logger = Logger.getLogger(Cell.class.getName());
    private static int instancesInitiated = 0;

    // Fields:
    private final int depth; // The level above root.
    private final CellParameters parameters;
    private final Cell[] subCells;
    private final EnumMap<Data, Object> data = new EnumMap<>(Data.class);
    private final int minX;
    private final int minY;
    private final ImmutableRectInt underlyingRectInt;
    private final EnumMap<Data, IntBinaryOperator> intDataMapperByKey = new EnumMap<>(Data.class);
    private final EnumMap<Data, BiIntToFloatFunction> floatDataMapperByKey = new EnumMap<>(Data.class);
    private final int id;

    // Variables.
    private boolean active = false;

    Cell(@NotNull CellParameters parameters) {

        id = instancesInitiated++;

        // Early exit.
        if (!parameters.hasSubCells()) {
            throw new IllegalArgumentException("Received invalid parameters: " + parameters);
        }

        underlyingRectInt = new ImmutableRectInt(parameters.x(), parameters.y(), parameters.width() - 1,
                parameters.height() - 1);
        minX = parameters.x();
        minY = parameters.y();

        this.parameters = parameters;

        final List<CellParameters> cellParametersList = IntStream.range(0, 4)
                .mapToObj(index -> CellParameters.builder(parameters.minimalCellSize())
                        .setX(parameters.x() + cellIndexToXCoordinate(index) * parameters.width() / 2)
                        .setY(parameters.y() + cellIndexToYCoordinate(index) * parameters.height() / 2)
                        .setWidth(parameters.width() / 2)
                        .setHeight(parameters.height() / 2)
                        .setFloatArrayPool(parameters.floatMatrixPool())
                        .setIntMatrixPool(parameters.intMatrixPool())
                        .build()
                ).toList();

        // One has subcomponents <=> all have subcomponents.
        if (cellParametersList.get(0).hasSubCells()) {

            subCells = cellParametersList.stream().map(Cell::new).toArray(Cell[]::new);

            final int midPointX = minX + parameters.width() / 2;
            final int midPointY = minY + parameters.height() / 2;

            // Glue functions together on boundaries:
            for (Data key : Data.values()) {
                switch (key.getType()) {
                    case FLOAT -> floatDataMapperByKey.put(key, (x, y) -> {
                        if (!contains(x, y)) return 0;

                        // See definition of adjacency in javadoc of this class.
                        if (x < midPointX) {
                            if (y < midPointY) {
                                // Upper left square
                                return subCells[0].getDataFloat(x, y, key);
                            } else {
                                // Lower left square
                                return subCells[2].getDataFloat(x, y, key);
                            }
                        } else {
                            if (y < midPointY) {
                                // Upper right square
                                return subCells[1].getDataFloat(x, y, key);
                            } else {
                                // Lower right square
                                return subCells[3].getDataFloat(x, y, key);
                            }
                        }
                    });
                    case INTEGER -> intDataMapperByKey.put(key, (x, y) -> {
                        if (!contains(x, y)) return 0;

                        // See definition of adjacency in javadoc of this class.
                        if (x < midPointX) {
                            if (y < midPointY) {
                                // Upper left square
                                return subCells[0].getDataInt(x, y, key);
                            } else {
                                // Lower left square
                                return subCells[2].getDataInt(x, y, key);
                            }
                        } else {
                            if (y < midPointY) {
                                // Upper right square
                                return subCells[1].getDataInt(x, y, key);
                            } else {
                                // Lower right square
                                return subCells[3].getDataInt(x, y, key);
                            }
                        }
                    });
                }
            }
        } else {
            subCells = null;
            for (Data key : Data.values()) {
                switch (key.getType()) {
                    case INTEGER -> intDataMapperByKey.put(key, (x, y) -> {
                        if (underlyingRectInt.contains(x, y)) {
                            final int i = x - minX, j = y - minY;
                            final IntMatrix dataForKey = (IntMatrix) data.get(key);
                            return (dataForKey != null) ? dataForKey.get(i, j) : 0;
                        } else return 0;
                    });
                    case FLOAT -> floatDataMapperByKey.put(key, (x, y) -> {
                        if (underlyingRectInt.contains(x, y)) {
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
        Cell cellStack = this;

        while (cellStack.subCells != null) {
            cellStack = cellStack.subCells[0];
            depth++;
        }
        this.depth = depth;
    }

    private static int cellIndexToXCoordinate(int index) {
        return index % 2;
    }

    private static int cellIndexToYCoordinate(int index) {
        if (index == 0 || index == 1) return 0;
        if (index == 2 || index == 3) return 1;
        throw new IllegalArgumentException();
    }

    @NotNull
    public static Cell makeRepresentationOf(int width,
                                            int height,
                                            int minimalCellSize,
                                            int poolObjectsAvailable) {
        final var intMatrixPool = new IntMatrixPool(poolObjectsAvailable, () -> new IntMatrix(minimalCellSize, minimalCellSize));
        final var floatMatrixPool = new FloatMatrixPool(poolObjectsAvailable, () -> new FloatMatrix(minimalCellSize, minimalCellSize));
        int x = minimalCellSize, y = minimalCellSize;
        do {
            x *= 2;
            y *= 2;
        } while (x < width || y < height);
        logger.log(Level.WARNING, "Creating components with dimensions {" + x + ", " + y + "}");
        return new Cell(CellParameters
                .builder(minimalCellSize)
                .setX(-x / 2)
                .setY(-y / 2)
                .setWidth(x)
                .setHeight(y)
                .setIntMatrixPool(intMatrixPool)
                .setFloatArrayPool(floatMatrixPool)
                .build());
    }


    public ImmutableRectInt getUnderlyingRectInt() {
        return underlyingRectInt;
    }

    /**
     * @return an unmodifiable view of sub-components.
     */
    public List<Cell> getSubComponents() {
        return List.of(subCells);
    }

    public void forEachMatching(Consumer<Cell> action, Predicate<Cell> condition) {
        if (condition.test(subCells[0])) action.accept(subCells[0]);
        if (condition.test(subCells[1])) action.accept(subCells[1]);
        if (condition.test(subCells[2])) action.accept(subCells[2]);
        if (condition.test(subCells[3])) action.accept(subCells[3]);
    }

    public boolean hasSubComponents() {
        return subCells != null;
    }

    public int getPositionX() {
        return parameters.x();
    }

    public int getPositionY() {
        return parameters.y();
    }

    public int getWidth() {
        return parameters.width();
    }

    public int getHeight() {
        return parameters.height();
    }

    /**
     * Returns true if [minX, maxX) and [minY, maxY) contains the point (x,y) evaluated.
     */
    public boolean contains(int x, int y) {
        return underlyingRectInt.contains(x, y);
    }

    public float getDataFloat(int x, int y, @NotNull Cell.Data key) {
        return floatDataMapperByKey.get(key).eval(x, y);
    }

    public int getDataInt(int x, int y, @NotNull Cell.Data key) {
        return intDataMapperByKey.get(key).apply(x, y);
    }


    @Override
    public int id() {
        return id;
    }

    public int getCollision(int x, int y) {
        return intDataMapperByKey.get(Data.COLLISION).apply(x, y);
    }

    public int getLadder(int x, int y) {
        return intDataMapperByKey.get(Data.LADDER).apply(x, y);
    }

    public float getVectorFieldX(int x, int y) {
        return floatDataMapperByKey.get(Data.VECTOR_FIELD_X).eval(x, y);
    }

    public float getVectorFieldY(int x, int y) {
        return floatDataMapperByKey.get(Data.VECTOR_FIELD_Y).eval(x, y);
    }

    public void modifyVectorFieldAt(int x, int y, FloatUnaryOperation transformX, FloatUnaryOperation transformY) {
        if (contains(x, y)) {
            if (hasSubComponents()) {
                for (Cell c : subCells) {
                    c.modifyVectorFieldAt(x, y, transformX, transformY);
                }
            } else {
                if (!data.containsKey(Data.VECTOR_FIELD_X)) {
                    data.put(Data.VECTOR_FIELD_X, parameters.floatMatrixPool().borrow());
                }
                if (!data.containsKey(Data.VECTOR_FIELD_Y)) {
                    data.put(Data.VECTOR_FIELD_Y, parameters.floatMatrixPool().borrow());
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
                for (Cell c : subCells) {
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
                for (Cell c : subCells) {
                    c.increment(x, y, key);
                }
            } else {
                if (!data.containsKey(key)) {
                    data.put(key, parameters.intMatrixPool().borrow());
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
            for (Cell c : subCells) {
                c.clearData();
            }
        } else {
            // Data which has been initialized (taken from the pool), is returned.
            {
                Object o = data.get(Data.COLLISION);
                if (o != null) {
                    parameters.intMatrixPool().giveBack((IntMatrix) o);
                }
            }
            {
                Object o = data.get(Data.LADDER);
                if (o != null) {
                    parameters.intMatrixPool().giveBack((IntMatrix) o);
                }
            }
            {
                Object o = data.get(Data.VECTOR_FIELD_X);
                if (o != null) {
                    parameters.floatMatrixPool().giveBack((FloatMatrix) o);
                }
            }
            {
                Object o = data.get(Data.VECTOR_FIELD_Y);
                if (o != null) {
                    parameters.floatMatrixPool().giveBack((FloatMatrix) o);
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
                ", y: " + underlyingRectInt.getMinY() + "}";
    }

    public void decrement(int x, int y, Data key) {
        if (contains(x, y)) {
            if (hasSubComponents()) {
                for (Cell c : subCells) {
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
                            String.format("The data of classification '%s' at position (%s,%s) for this sub-component was empty. "
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


    public boolean isActive() {
        return active;
    }

    public void setActive(boolean b) {
        this.active = b;
    }

    public boolean isInactive() {
        return !active;
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
