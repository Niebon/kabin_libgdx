package dev.kabin.util.geometry;

import dev.kabin.spring.SpringHelpers;
import dev.kabin.util.geometry.points.PointFloat;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;


class AlgorithmChiShapeTest {


    @Test
    void square() {
        var points = List.of(
                PointFloat.immutable(0, 0),
                PointFloat.immutable(1, 0),
                PointFloat.immutable(1, 1),
                PointFloat.immutable(0, 1));

        var res = AlgorithmChiShape.on(points, 0.5f);
        System.out.println(res);

        SpringHelpers.makePolygons(points, res.boundary());
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}