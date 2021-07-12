package dev.kabin.util.geometry;

import dev.kabin.util.geometry.points.PointFloatImmutable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Arrays;
import java.util.List;

public final class PolygonImmutable implements Polygon {

    private final EdgeImmutable[] data;

    PolygonImmutable(EdgeImmutable[] data) {
        this.data = data;
    }

    @Override
    @UnmodifiableView
    public List<PointFloatImmutable> boundary() {
        return Arrays.stream(data).map(Edge::start).map(PointFloatImmutable.class::cast).toList();
    }

    @Override
	public String toString() {
		return "Polygon{" +
				"data=" + Arrays.toString(data) +
				'}';
	}


}
