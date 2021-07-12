package dev.kabin.util.geometry;

import dev.kabin.util.geometry.points.PointFloat;
import org.junit.jupiter.api.Assertions;
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
        Assertions.assertEquals(
                List.of(
                        List.of(
                                PointFloat.immutable(1.0f, 1.0f),
                                PointFloat.immutable(0.0f, 0.0f),
                                PointFloat.immutable(1.0f, 0.0f)
                        ),
                        List.of(
                                PointFloat.immutable(0.0f, 1.0f),
                                PointFloat.immutable(1.0f, 1.0f),
                                PointFloat.immutable(1.0f, 0.0f)
                        )
                ), triangulation.stream().map(Triangle::boundary).toList()
        );
    }

}