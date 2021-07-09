package dev.kabin.util.geometry;

import dev.kabin.util.geometry.points.ImmutablePointFloat;
import dev.kabin.util.geometry.points.PointFloat;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public final class Polygon {

	private final Segment[] data;

	private Polygon(Segment[] data) {
		this.data = data;
	}

	public static Builder builder() {
		return new Builder();
	}

	@UnmodifiableView
	public List<ImmutablePointFloat> boundary() {
		return Arrays.stream(data).map(Segment::start).map(PointFloat::immutable).toList();
	}

	@Override
	public String toString() {
		return "Polygon{" +
				"data=" + Arrays.toString(data) +
				'}';
	}

	/**
	 * @return a deep clone of this polygon.
	 */
	@SuppressWarnings("MethodDoesntCallSuperMethod")
	public Polygon clone() {
		return new Polygon(Arrays.stream(data).map(Segment::clone).toArray(Segment[]::new));
	}

	public static class Builder {

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

		public Polygon buildModifiable() {
			final Segment[] data = IntStream.range(0, builderHelper.size())
					.mapToObj(i -> {
						var first = builderHelper.get(Math.floorMod(i, builderHelper.size()));
						var second = builderHelper.get(Math.floorMod(i + 1, builderHelper.size()));
						return new SegmentModifiable(PointFloat.modifiable(first), PointFloat.modifiable(second));
					})
					.toArray(Segment[]::new);
			return new Polygon(data);
		}

		public Polygon buildImmutable() {
			final Segment[] data = IntStream.range(0, builderHelper.size())
					.mapToObj(i -> {
						var first = builderHelper.get(Math.floorMod(i, builderHelper.size()));
						var second = builderHelper.get(Math.floorMod(i + 1, builderHelper.size()));
						return new SegmentImmutable(PointFloat.immutable(first), PointFloat.immutable(second));
					})
					.toArray(Segment[]::new);
			return new Polygon(data);
		}

	}
}
