package dev.kabin.util.geometry;

import dev.kabin.util.geometry.points.PointFloat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class AlgorithmBowyerWatson {

    // https://en.wikipedia.org/wiki/Bowyer%E2%80%93Watson_algorithm
    // See under pseudocode.
    public static Collection<Triangle> calculateDelaunayTriangulation(Collection<? extends PointFloat> pointList) {

        class Constant {
            final static float EPSILON = 1f;
        }

        // Find minimal and maximal coordinates among points to be triangulated:
        final float xMin = (float) pointList.stream().mapToDouble(PointFloat::x).min().orElseThrow() - Constant.EPSILON;
        final float xMax = (float) pointList.stream().mapToDouble(PointFloat::x).max().orElseThrow() + Constant.EPSILON;
        final float yMin = (float) pointList.stream().mapToDouble(PointFloat::y).min().orElseThrow() - Constant.EPSILON;
        final float yMax = (float) pointList.stream().mapToDouble(PointFloat::x).max().orElseThrow() + Constant.EPSILON;

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
        final var superTriangle = Triangle.immutable(
                xMin, yMin,
                2 * xMax, yMin,
                xMin, 2 * yMax
        );

        final var triangulation = new ArrayList<Triangle>();
        triangulation.add(superTriangle);
        for (var point : pointList) {
            final var badTriangles = new HashSet<Triangle>();
            for (var triangle : triangulation) {
                if (Make.circumCircleOf(triangle).contains(point)) {
                    badTriangles.add(triangle);
                }
            }
            final var polygon = new HashSet<Edge>();
            for (var triangle : badTriangles) {
                triangle.forEachEdge(edge -> {
                    // If edge is not shared by any other triangles in badTriangles:
                    if (badTriangles.stream().filter(t -> t != triangle).noneMatch(t -> t.hasEdge(edge))) {
                        polygon.add(edge);
                    }
                });
            }
            triangulation.removeIf(badTriangles::contains);
            for (var edge : polygon) {
                triangulation.add(Triangle.immutable(edge.startX(), edge.startY(), edge.endX(), edge.endY(), point.x(), point.y()));
            }
        }
        superTriangle.forEachVertex(v -> triangulation.removeIf(t -> t.hasVertex(v)));
        return triangulation;
    }

}
