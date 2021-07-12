package dev.kabin.util.geometry;

import dev.kabin.util.geometry.points.PointFloat;
import org.junit.jupiter.api.Test;

import java.util.List;

class BowyerWatsonAlgorithmTest {


    @Test
    void square() {
        var triangulation = BowyerWatsonAlgorithm.calculateDelaunayTriangulation(List.of(
                PointFloat.immutable(0, 0),
                PointFloat.immutable(1, 0),
                PointFloat.immutable(1, 1),
                PointFloat.immutable(0, 1)
        ));
        System.out.println(triangulation);
    }

}