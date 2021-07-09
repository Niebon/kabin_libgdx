package dev.kabin.util.geometry;

import dev.kabin.util.geometry.points.PointFloat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class GrahamScanAlgorithmTest {


    @Test
    public void square() {
        var convexRepresentation = GrahamScanAlgorithm.on(List.of(
                PointFloat.immutable(0, 0),
                PointFloat.immutable(0, 1),
                PointFloat.immutable(1, 1),
                PointFloat.immutable(1, 0)));
        System.out.print(convexRepresentation);
        Assertions.assertEquals(List.of(
                PointFloat.immutable(0, 0),
                PointFloat.immutable(1, 0),
                PointFloat.immutable(1, 1),
                PointFloat.immutable(0, 1)), convexRepresentation.boundary());

    }

    @Test
    public void squareWithInterior() {
        var convexRepresentation = GrahamScanAlgorithm.on(List.of(
                // Boundary
                PointFloat.immutable(0, 0),
                PointFloat.immutable(0, 1),
                PointFloat.immutable(1, 1),
                PointFloat.immutable(1, 0),

                // Interior
                PointFloat.immutable(0.5f, 0.5f)));
        System.out.print(convexRepresentation);
        Assertions.assertEquals(List.of(
                PointFloat.immutable(0, 0),
                PointFloat.immutable(1, 0),
                PointFloat.immutable(1, 1),
                PointFloat.immutable(0, 1)), convexRepresentation.boundary());

    }

    @Test
    public void squareWithRedunantBoundaryPoints() {
        var convexRepresentation = GrahamScanAlgorithm.on(List.of(
                // Boundary
                PointFloat.immutable(0, 0),
                PointFloat.immutable(0, 1),
                PointFloat.immutable(1, 1),
                PointFloat.immutable(1, 0),

                // A redundant boundary point
                PointFloat.immutable(1, 0.5f)));
        System.out.print(convexRepresentation);
        Assertions.assertEquals(List.of(
                PointFloat.immutable(0, 0),
                PointFloat.immutable(1, 0),
                PointFloat.immutable(1, 1),
                PointFloat.immutable(0, 1)), convexRepresentation.boundary());

    }

    @Test
    public void squareUnionOutlier() {
        var convexRepresentation = GrahamScanAlgorithm.on(List.of(
                // Boundary
                PointFloat.immutable(0, 0),
                PointFloat.immutable(0, 1),
                PointFloat.immutable(1, 1),
                PointFloat.immutable(1, 0),

                // A redundant boundary point
                PointFloat.immutable(2, 0)));
        System.out.print(convexRepresentation);
        Assertions.assertEquals(List.of(
                PointFloat.immutable(0, 0),
                PointFloat.immutable(2, 0),
                PointFloat.immutable(1, 1),
                PointFloat.immutable(0, 1)), convexRepresentation.boundary());

    }


}