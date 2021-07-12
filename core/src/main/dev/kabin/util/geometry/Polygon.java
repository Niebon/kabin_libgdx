package dev.kabin.util.geometry;

import dev.kabin.util.geometry.points.PointFloat;
import dev.kabin.util.geometry.points.PointFloatImmutable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public interface Polygon {

    static PolygonBuilder builder() {
        return new PolygonBuilder();
    }

    @UnmodifiableView List<PointFloatImmutable> boundary();

}

class PolygonBuilder {

    private final List<PointFloat> builderHelper = new ArrayList<>();

    PolygonBuilder() {

    }

    public PolygonBuilder add(float x, float y) {
        builderHelper.add(PointFloat.modifiable(x, y));
        return this;
    }

    public PolygonBuilder add(PointFloat p) {
        builderHelper.add(p);
        return this;
    }

    public PolygonBuilder addAll(List<? extends PointFloat> data) {
        builderHelper.addAll(data);
        return this;
    }

    public PolygonModifiable buildModifiable() {
        return new PolygonModifiable(IntStream.range(0, builderHelper.size())
                .mapToObj(i -> {
                    var first = builderHelper.get(Math.floorMod(i, builderHelper.size()));
                    var second = builderHelper.get(Math.floorMod(i + 1, builderHelper.size()));
                    return new EdgeModifiable(PointFloat.modifiable(first), PointFloat.modifiable(second));
                })
                .toArray(EdgeModifiable[]::new));
    }

    public PolygonImmutable buildImmutable() {
        return new PolygonImmutable(IntStream.range(0, builderHelper.size())
                .mapToObj(i -> {
                    var first = builderHelper.get(Math.floorMod(i, builderHelper.size()));
                    var second = builderHelper.get(Math.floorMod(i + 1, builderHelper.size()));
                    return new EdgeImmutable(PointFloat.immutable(first), PointFloat.immutable(second));
                })
                .toArray(EdgeImmutable[]::new));
    }

}
