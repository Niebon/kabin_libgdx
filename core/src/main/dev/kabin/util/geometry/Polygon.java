package dev.kabin.util.geometry;

import dev.kabin.util.geometry.points.ImmutablePointFloat;
import dev.kabin.util.geometry.points.PointFloat;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public interface Polygon {

    static ModifiablePolygon.Builder builder() {
        return new ModifiablePolygon.Builder();
    }

    @UnmodifiableView List<ImmutablePointFloat> boundary();

    class Builder {

        private final List<PointFloat> builderHelper = new ArrayList<>();

        public Builder add(float x, float y) {
            builderHelper.add(PointFloat.modifiable(x, y));
            return this;
        }

        public Builder add(PointFloat p) {
            builderHelper.add(p);
            return this;
        }

        public Builder addAll(List<PointFloat> data) {
            builderHelper.addAll(data);
            return this;
        }

        public ModifiablePolygon buildModifiable() {
            final SegmentModifiable[] data = IntStream.range(0, builderHelper.size())
                    .mapToObj(i -> {
                        var first = builderHelper.get(Math.floorMod(i, builderHelper.size()));
                        var second = builderHelper.get(Math.floorMod(i + 1, builderHelper.size()));
                        return new SegmentModifiable(PointFloat.modifiable(first), PointFloat.modifiable(second));
                    })
                    .toArray(SegmentModifiable[]::new);
            return new ModifiablePolygon(data);
        }

        public ImmutablePolygon buildImmutable() {
            final SegmentImmutable[] data = IntStream.range(0, builderHelper.size())
                    .mapToObj(i -> {
                        var first = builderHelper.get(Math.floorMod(i, builderHelper.size()));
                        var second = builderHelper.get(Math.floorMod(i + 1, builderHelper.size()));
                        return new SegmentImmutable(PointFloat.immutable(first), PointFloat.immutable(second));
                    })
                    .toArray(SegmentImmutable[]::new);
            return new ImmutablePolygon(data);
        }

    }
}
