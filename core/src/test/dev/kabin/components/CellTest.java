package dev.kabin.components;

import dev.kabin.util.functioninterfaces.FloatUnaryOperation;
import dev.kabin.util.points.PointInt;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class CellTest {

    @Test
    public void componentsRecursionTerminatesAndHasCorrectDimensions() {
        final Cell mainCell = Cell.make(
                CellParameters.builder()
                        .setX(0)
                        .setY(0)
                        .setWidth(CellParameters.COARSENESS_PARAMETER * 2)
                        .setHeight(CellParameters.COARSENESS_PARAMETER * 2)
                        .setScaleFactor(1)
                        .build()
        );

        // Check sub-component x-position
        Assertions.assertEquals(0, mainCell.getSubComponents().get(0).getPositionX());
        Assertions.assertEquals(CellParameters.COARSENESS_PARAMETER, mainCell.getSubComponents().get(1).getPositionX());
        Assertions.assertEquals(0, mainCell.getSubComponents().get(2).getPositionX());
        Assertions.assertEquals(CellParameters.COARSENESS_PARAMETER, mainCell.getSubComponents().get(3).getPositionX());

        // Check sub-component y-position
        Assertions.assertEquals(0, mainCell.getSubComponents().get(0).getPositionY());
        Assertions.assertEquals(0, mainCell.getSubComponents().get(1).getPositionY());
        Assertions.assertEquals(CellParameters.COARSENESS_PARAMETER, mainCell.getSubComponents().get(2).getPositionY());
        Assertions.assertEquals(CellParameters.COARSENESS_PARAMETER, mainCell.getSubComponents().get(3).getPositionY());

        // Check that sub-components are final
        Assertions.assertFalse(mainCell.getSubComponents().get(0).hasSubComponents());
        Assertions.assertFalse(mainCell.getSubComponents().get(1).hasSubComponents());
        Assertions.assertFalse(mainCell.getSubComponents().get(2).hasSubComponents());
        Assertions.assertFalse(mainCell.getSubComponents().get(3).hasSubComponents());
    }

    @Test
    public void dataRemovalThrowsForEmptyLists() {
        final Cell mainCell = Cell.make(
                CellParameters.builder()
                        .setX(0)
                        .setY(0)
                        .setWidth(CellParameters.COARSENESS_PARAMETER * 2)
                        .setHeight(CellParameters.COARSENESS_PARAMETER * 2)
                        .setScaleFactor(1)
                        .build()
        );

        Arrays.stream(Cell.Data.values()).filter(d -> d.getType() == Cell.Data.PrimitiveType.INTEGER).forEach(type ->
                Assertions.assertThrows(RuntimeException.class, () -> mainCell.decrement(0, 0, type)));
    }

    @Test
    public void dataInsertionIncreaseAndRetrieve() {

        final int width = CellParameters.COARSENESS_PARAMETER * 8,
                height = CellParameters.COARSENESS_PARAMETER * 8;

        final Cell mainCell = Cell.make(
                CellParameters.builder()
                        .setX(0)
                        .setY(0)
                        .setWidth(width)
                        .setHeight(height)
                        .setScaleFactor(1)
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