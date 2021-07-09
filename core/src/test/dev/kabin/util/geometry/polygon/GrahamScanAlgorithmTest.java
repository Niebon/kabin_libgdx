package dev.kabin.util.geometry.polygon;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class GrahamScanAlgorithmTest {


    @Test
    public void square() {
        var convexRepresentation = GrahamScanAlgorithm.on(List.of(
                Point.of(0, 0),
                Point.of(0, 1),
                Point.of(1, 1),
                Point.of(1, 0)));
        System.out.print(convexRepresentation);
        Assertions.assertEquals(List.of(
                Point.of(0, 0),
                Point.of(1, 0),
                Point.of(1, 1),
                Point.of(0, 1)), convexRepresentation.boundary());

    }

    @Test
    public void squareWithInterior() {
        var convexRepresentation = GrahamScanAlgorithm.on(List.of(
                // Boundary
                Point.of(0, 0),
                Point.of(0, 1),
                Point.of(1, 1),
                Point.of(1, 0),

                // Interior
                Point.of(0.5f, 0.5f)));
        System.out.print(convexRepresentation);
        Assertions.assertEquals(List.of(
                Point.of(0, 0),
                Point.of(1, 0),
                Point.of(1, 1),
                Point.of(0, 1)), convexRepresentation.boundary());

    }

    @Test
    public void squareWithRedunantBoundaryPoints() {
        var convexRepresentation = GrahamScanAlgorithm.on(List.of(
                // Boundary
                Point.of(0, 0),
                Point.of(0, 1),
                Point.of(1, 1),
                Point.of(1, 0),

                // A redundant boundary point
                Point.of(1, 0.5f)));
        System.out.print(convexRepresentation);
        Assertions.assertEquals(List.of(
                Point.of(0, 0),
                Point.of(1, 0),
                Point.of(1, 1),
                Point.of(0, 1)), convexRepresentation.boundary());

    }

    @Test
    public void squareUnionOutlier() {
        var convexRepresentation = GrahamScanAlgorithm.on(List.of(
                // Boundary
                Point.of(0, 0),
                Point.of(0, 1),
                Point.of(1, 1),
                Point.of(1, 0),

                // A redundant boundary point
                Point.of(2, 0)));
        System.out.print(convexRepresentation);
        Assertions.assertEquals(List.of(
                Point.of(0, 0),
                Point.of(2, 0),
                Point.of(1, 1),
                Point.of(0, 1)), convexRepresentation.boundary());

    }


}