package dev.kabin.util.cell;

import dev.kabin.components.worldmodel.FloatMatrixPool;
import dev.kabin.components.worldmodel.IntMatrixPool;
import dev.kabin.util.functioninterfaces.FloatUnaryOperation;
import dev.kabin.util.linalg.FloatMatrix;
import dev.kabin.util.linalg.IntMatrix;
import dev.kabin.util.points.PointInt;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class CellTest {

    public static final int MINIMAL_CELL_SIZE = 128;

    @Test
    public void componentsRecursionTerminatesAndHasCorrectDimensions() {
        final var mainCell = new Cell(
                CellParameters.builder(128)
                        .setX(0)
                        .setY(0)
                        .setWidth(MINIMAL_CELL_SIZE * 2)
                        .setHeight(128 * 2)
                        .setScaleFactor(1)
                        .setIntMatrixPool(new IntMatrixPool(10, () -> new IntMatrix(128, 128)))
                        .setFloatArrayPool(new FloatMatrixPool(10, () -> new FloatMatrix(128, 128)))
                        .build()
        );

        // Check sub-component x-position
        Assertions.assertEquals(0, mainCell.getSubComponents().get(0).getPositionX());
        Assertions.assertEquals(128, mainCell.getSubComponents().get(1).getPositionX());
        Assertions.assertEquals(0, mainCell.getSubComponents().get(2).getPositionX());
        Assertions.assertEquals(128, mainCell.getSubComponents().get(3).getPositionX());

        // Check sub-component y-position
        Assertions.assertEquals(0, mainCell.getSubComponents().get(0).getPositionY());
        Assertions.assertEquals(0, mainCell.getSubComponents().get(1).getPositionY());
        Assertions.assertEquals(128, mainCell.getSubComponents().get(2).getPositionY());
        Assertions.assertEquals(128, mainCell.getSubComponents().get(3).getPositionY());

        // Check that sub-components are final
        Assertions.assertFalse(mainCell.getSubComponents().get(0).hasSubComponents());
        Assertions.assertFalse(mainCell.getSubComponents().get(1).hasSubComponents());
        Assertions.assertFalse(mainCell.getSubComponents().get(2).hasSubComponents());
        Assertions.assertFalse(mainCell.getSubComponents().get(3).hasSubComponents());
    }

    @Test
    public void dataRemovalThrowsForEmptyLists() {
        final var mainCell = new Cell(
                CellParameters.builder(128)
                        .setX(0)
                        .setY(0)
                        .setWidth(MINIMAL_CELL_SIZE * 2)
                        .setHeight(128 * 2)
                        .setScaleFactor(1)
                        .setIntMatrixPool(new IntMatrixPool(10, () -> new IntMatrix(128, 128)))
                        .setFloatArrayPool(new FloatMatrixPool(10, () -> new FloatMatrix(128, 128)))
                        .build()
        );

        Arrays.stream(Cell.Data.values()).filter(d -> d.getType() == Cell.Data.PrimitiveType.INTEGER).forEach(type ->
                Assertions.assertThrows(RuntimeException.class, () -> mainCell.decrement(0, 0, type)));
    }

    @Test
    public void dataInsertionIncreaseAndRetrieve() {

        final int width = MINIMAL_CELL_SIZE * 8,
                height = MINIMAL_CELL_SIZE * 8;

        final var mainCell = new Cell(
                CellParameters.builder(128)
                        .setX(0)
                        .setY(0)
                        .setWidth(width)
                        .setHeight(height)
                        .setScaleFactor(1)
                        .setIntMatrixPool(new IntMatrixPool(128, () -> new IntMatrix(128, 128)))
                        .setFloatArrayPool(new FloatMatrixPool(128, () -> new FloatMatrix(128, 128)))
                        .build()
        );

        final Random r = new Random();
        final List<PointInt> points = IntStream.range(0, 100000)
                .mapToObj(i -> PointInt.modifiable(r.nextInt(width), r.nextInt(height))).collect(Collectors.toList());

        for (Cell.Data type : Cell.Data.values()) {
            for (PointInt pointInt : points) {
                final int x = pointInt.x(), y = pointInt.y();
                {

                    if (type.getType() == Cell.Data.PrimitiveType.INTEGER) {
                        mainCell.increment(x, y, type);
                        final Object expected = 1;
                        Assertions.assertEquals(expected, mainCell.getDataInt(pointInt.x(), pointInt.y(), type),
                                "Got error while retrieving x, y = " + x + ", " + y + ", of type " + type.name());
                    }


                    if (type == Cell.Data.VECTOR_FIELD_X || type == Cell.Data.VECTOR_FIELD_Y) {
                        FloatUnaryOperation operateX = type == Cell.Data.VECTOR_FIELD_X ? f -> f + 1f : FloatUnaryOperation.TRIVIAL;
                        FloatUnaryOperation operateY = type == Cell.Data.VECTOR_FIELD_Y ? f -> f + 1f : FloatUnaryOperation.TRIVIAL;
                        mainCell.modifyVectorFieldAt(x, y, operateX, operateY);
                        final float expected = 1.0f;
                        Assertions.assertEquals(expected, mainCell.getDataFloat(pointInt.x(), pointInt.y(), type), 0.01f,
                                "Got error while retrieving x, y = " + x + ", " + y + ", of type " + type.name());
                    }
                }
                {

                    if (type.getType() == Cell.Data.PrimitiveType.INTEGER) {
                        mainCell.decrement(x, y, type);
                        final Object expected = 0;
                        Assertions.assertEquals(expected, mainCell.getDataInt(pointInt.x(), pointInt.y(), type),
                                "Got error while retrieving x, y = " + x + ", " + y + ", of type " + type.name());
                    }


                    if (type == Cell.Data.VECTOR_FIELD_X || type == Cell.Data.VECTOR_FIELD_Y) {
                        FloatUnaryOperation operateX = type == Cell.Data.VECTOR_FIELD_X ? f -> f - 1f : FloatUnaryOperation.TRIVIAL;
                        FloatUnaryOperation operateY = type == Cell.Data.VECTOR_FIELD_Y ? f -> f - 1f : FloatUnaryOperation.TRIVIAL;
                        mainCell.modifyVectorFieldAt(x, y, operateX, operateY);
                        final float expected = 0.0f;
                        Assertions.assertEquals(expected, mainCell.getDataFloat(pointInt.x(), pointInt.y(), type), 0.01f,
                                "Got error while retrieving x, y = " + x + ", " + y + ", of type " + type.name());
                    }
                }
            }
        }
    }
}