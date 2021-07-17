package dev.kabin.util.geometry;

import dev.kabin.spring.SpringHelpers;
import dev.kabin.util.geometry.points.PointFloat;
import dev.kabin.util.geometry.points.PointFloatImmutable;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

class AlgorithmBowyerWatsonTest {


    @Test
    void square() {
        List<PointFloatImmutable> points = List.of(
                PointFloat.immutable(0, 0),
                PointFloat.immutable(1, 0),
                PointFloat.immutable(1, 1),
                PointFloat.immutable(0, 1)
        );
        var triangulation = AlgorithmBowyerWatson.calculateDelaunayTriangulation(points);
        System.out.println(triangulation);
//        Assertions.assertEquals(
//                List.of(
//                        List.of(
//                                PointFloat.immutable(1.0f, 1.0f),
//                                PointFloat.immutable(0.0f, 0.0f),
//                                PointFloat.immutable(1.0f, 0.0f)
//                        ),
//                        List.of(
//                                PointFloat.immutable(0.0f, 1.0f),
//                                PointFloat.immutable(1.0f, 1.0f),
//                                PointFloat.immutable(1.0f, 0.0f)
//                        )
//                ), triangulation.stream().map(Triangle::boundary).toList()
//        );

        SpringHelpers.makePolygons(triangulation.stream().map(Triangle::boundary).toArray(List[]::new));
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}