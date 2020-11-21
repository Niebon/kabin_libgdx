package dev.kabin.components;

import dev.kabin.utilities.functioninterfaces.FloatUnaryOperation;
import dev.kabin.utilities.points.PointInt;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class ComponentTest {

    @Test
    public void componentsRecursionTerminatesAndHasCorrectDimensions() {
        final Component mainComponent = new Component(
                new ComponentParameters()
                        .setX(0)
                        .setY(0)
                        .setWidth(ComponentParameters.COARSENESS_PARAMETER * 2)
                        .setHeight(ComponentParameters.COARSENESS_PARAMETER * 2)
                        .setScaleFactor(1)
        );

        // Check sub-component x-position
        Assertions.assertEquals(0, mainComponent.getSubComponents().get(0).getPositionX());
        Assertions.assertEquals(ComponentParameters.COARSENESS_PARAMETER, mainComponent.getSubComponents().get(1).getPositionX());
        Assertions.assertEquals(0, mainComponent.getSubComponents().get(2).getPositionX());
        Assertions.assertEquals(ComponentParameters.COARSENESS_PARAMETER, mainComponent.getSubComponents().get(3).getPositionX());

        // Check sub-component y-position
        Assertions.assertEquals(0, mainComponent.getSubComponents().get(0).getPositionY());
        Assertions.assertEquals(0, mainComponent.getSubComponents().get(1).getPositionY());
        Assertions.assertEquals(ComponentParameters.COARSENESS_PARAMETER, mainComponent.getSubComponents().get(2).getPositionY());
        Assertions.assertEquals(ComponentParameters.COARSENESS_PARAMETER, mainComponent.getSubComponents().get(3).getPositionY());

        // Check that sub-components are final
        Assertions.assertFalse(mainComponent.getSubComponents().get(0).hasSubComponents());
        Assertions.assertFalse(mainComponent.getSubComponents().get(1).hasSubComponents());
        Assertions.assertFalse(mainComponent.getSubComponents().get(2).hasSubComponents());
        Assertions.assertFalse(mainComponent.getSubComponents().get(3).hasSubComponents());
    }

    @Test
    public void dataRemovalThrowsForEmptyLists() {
        final Component mainComponent = new Component(
                new ComponentParameters()
                        .setX(0)
                        .setY(0)
                        .setWidth(ComponentParameters.COARSENESS_PARAMETER * 2)
                        .setHeight(ComponentParameters.COARSENESS_PARAMETER * 2)
                        .setScaleFactor(1)
        );

        Arrays.stream(Component.Data.values()).filter(d -> d.getType() == Component.Data.Type.INTEGER).forEach(type ->
                Assertions.assertThrows(RuntimeException.class, () -> mainComponent.decrement(0, 0, type)));
    }

    @Test
    public void dataInsertionIncreaseAndRetrieve() {

        final int width = ComponentParameters.COARSENESS_PARAMETER * 8,
                height = ComponentParameters.COARSENESS_PARAMETER * 8;

        final Component mainComponent = new Component(
                new ComponentParameters()
                        .setX(0)
                        .setY(0)
                        .setWidth(width)
                        .setHeight(height)
                        .setScaleFactor(1)
        );

        final Random r = new Random();
        final List<PointInt> points = IntStream.range(0, 100000)
                .mapToObj(i -> new PointInt(r.nextInt(width), r.nextInt(height))).collect(Collectors.toList());

        for (Component.Data type : Component.Data.values()) {
            for (PointInt pointInt : points) {
                final int x = pointInt.x(), y = pointInt.y();
                {

                    if (type.getType() == Component.Data.Type.INTEGER) {
                        mainComponent.increment(x, y, type);
                        final Object expected = 1;
                        Assertions.assertEquals(expected, mainComponent.getDataInt(pointInt.x(), pointInt.y(), type),
                                "Got error while retrieving x, y = " + x + ", " + y + ", of type " + type.name());
                    }


                    if (type == Component.Data.VECTOR_FIELD_X || type == Component.Data.VECTOR_FIELD_Y) {
                        FloatUnaryOperation operateX = type == Component.Data.VECTOR_FIELD_X ? f -> f + 1f : FloatUnaryOperation.TRIVIAL;
                        FloatUnaryOperation operateY = type == Component.Data.VECTOR_FIELD_Y ? f -> f + 1f : FloatUnaryOperation.TRIVIAL;
                        mainComponent.modifyVectorFieldAt(x, y, operateX, operateY);
                        final Object expected = 1.0;
                        Assertions.assertEquals(expected, mainComponent.getDataDouble(pointInt.x(), pointInt.y(), type),
                                "Got error while retrieving x, y = " + x + ", " + y + ", of type " + type.name());
                    }
                }
                {

                    if (type.getType() == Component.Data.Type.INTEGER) {
                        mainComponent.decrement(x, y, type);
                        final Object expected = 0;
                        Assertions.assertEquals(expected, mainComponent.getDataInt(pointInt.x(), pointInt.y(), type),
                                "Got error while retrieving x, y = " + x + ", " + y + ", of type " + type.name());
                    }


                    if (type == Component.Data.VECTOR_FIELD_X || type == Component.Data.VECTOR_FIELD_Y) {
                        FloatUnaryOperation operateX = type == Component.Data.VECTOR_FIELD_X ? f -> f - 1f : FloatUnaryOperation.TRIVIAL;
                        FloatUnaryOperation operateY = type == Component.Data.VECTOR_FIELD_Y ? f -> f - 1f : FloatUnaryOperation.TRIVIAL;
                        mainComponent.modifyVectorFieldAt(x, y, operateX, operateY);
                        final Object expected = 0.0;
                        Assertions.assertEquals(expected, mainComponent.getDataDouble(pointInt.x(), pointInt.y(), type),
                                "Got error while retrieving x, y = " + x + ", " + y + ", of type " + type.name());
                    }
                }
            }
        }
    }
}