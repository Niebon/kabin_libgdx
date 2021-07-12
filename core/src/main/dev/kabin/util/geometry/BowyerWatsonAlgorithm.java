package dev.kabin.util.geometry;

import dev.kabin.util.geometry.points.PointFloat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class BowyerWatsonAlgorithm {

    // https://en.wikipedia.org/wiki/Bowyer%E2%80%93Watson_algorithm
    // See under pseudocode.
    public static Collection<Triangle> calculateDelaunayTriangulation(Collection<PointFloat> pointList) {

        class Constant {
            final static float EPSILON = 10e-5f;
        }

        // Find minimal and maximal coordinates among points to be triangulated:
        float xMin = (float) pointList.stream().mapToDouble(PointFloat::x).min().orElseThrow() - Constant.EPSILON;
        float xMax = (float) pointList.stream().mapToDouble(PointFloat::x).max().orElseThrow() + Constant.EPSILON;
        float yMin = (float) pointList.stream().mapToDouble(PointFloat::y).min().orElseThrow() - Constant.EPSILON;
        float yMax = (float) pointList.stream().mapToDouble(PointFloat::x).max().orElseThrow() + Constant.EPSILON;

        // The below super triangle contains all points.
        // Proof by image:
        //
        // |⟍
        // |__ ⟍
        // |    |⟍
        // |____|___⟍
        //
        // The rectangle defined by minimal/maximal coordinates contains all points,
        // so the enclosing triangle contains all points too.
        // Add epsilon to subdue any round-off errors.
        var superTriangle = Triangle.immutable(
                xMin, yMin,
                xMin, 2 * yMax,
                2 * xMax, yMin
        );

        final var triangulation = new ArrayList<Triangle>();
        triangulation.add(superTriangle);
        for (var point : pointList) {
            var badTriangles = new HashSet<Triangle>();
            for (var triangle : triangulation) {
                if (Make.circumCircleOf(triangle).contains(point.x(), point.y())) {
                    badTriangles.add(triangle);
                }
            }
            var polygon = new HashSet<Edge>();
            for (var triangle : badTriangles) {
                triangle.forEachEdge(e -> {
                    if (badTriangles.stream().noneMatch(t -> t.hasEdge(e))) {
                        polygon.add(e);
                    }
                });
            }
            triangulation.removeIf(badTriangles::contains);
            for (var edge : polygon) {
                triangulation.add(Triangle.immutable(point.x(), point.y(), edge.startX(), edge.startY(), edge.endX(), edge.endY()));
            }
        }
        superTriangle.forEachEdge(v -> triangulation.removeIf(t -> t.hasEdge(v)));
        return triangulation;
    }

}
