package dev.kabin.util.geometry;

import dev.kabin.util.geometry.points.PointFloat;

import java.util.ArrayList;
import java.util.Collection;

public class BowyerWatsonAlgorithm {

    public static Collection<Triangle> calculateDelaunayTriangulation(Collection<PointFloat> points) {

        class Constant {
            final static float EPSILON = 10e-5f;
        }


        float xMin = (float) points.stream().mapToDouble(PointFloat::x).min().orElseThrow() - Constant.EPSILON;
        float xMax = (float) points.stream().mapToDouble(PointFloat::x).max().orElseThrow() + Constant.EPSILON;
        float yMin = (float) points.stream().mapToDouble(PointFloat::y).min().orElseThrow() - Constant.EPSILON;
        float yMax = (float) points.stream().mapToDouble(PointFloat::x).max().orElseThrow() + Constant.EPSILON;

        // Contains all points.
        var superTriangle = Triangle.immutable(
                xMin, yMin,
                xMin, 2 * yMax,
                2 * xMax, yMin
        );

        final var triangulation = new ArrayList<Triangle>();
        triangulation.add(superTriangle);
        for (var point : points) {

        }
        triangulation.removeIf(t -> t.hasVertex(superTriangle.e1()));
        return triangulation;
    }

}
