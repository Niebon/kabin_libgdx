package dev.kabin.util.geometry;

import dev.kabin.util.geometry.points.PointFloat;
import dev.kabin.util.geometry.points.PointFloatImmutable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Arrays;
import java.util.List;

public final class PolygonModifiable implements Polygon, RigidTransformations {

    private final SegmentModifiable[] data;

    PolygonModifiable(SegmentModifiable[] data) {
        this.data = data;
    }

    @Override
    @UnmodifiableView
    public List<PointFloatImmutable> boundary() {
		return Arrays.stream(data).map(Segment::start).map(PointFloat::immutable).toList();
    }

    @Override
    public String toString() {
        return "Polygon{" +
                "data=" + Arrays.toString(data) +
                '}';
    }


    @Override
    public void rotate(float pivotX, float pivotY, double angleRad) {
        for (SegmentModifiable datum : data) {
            datum.start().rotate(pivotX, pivotY, angleRad);
        }
    }

    @Override
    public void translate(float deltaX, float deltaY) {
        for (SegmentModifiable datum : data) {
            datum.start().translate(deltaX, deltaY);
        }
    }
}
