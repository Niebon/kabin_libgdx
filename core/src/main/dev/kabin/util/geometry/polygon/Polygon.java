package dev.kabin.util.geometry.polygon;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class Polygon {

	private final Segment[] data;

	private Polygon(Segment[] data) {
		this.data = data;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private final List<Point> builderHelper = new ArrayList<>();

		public Builder add(float x, float y) {
			builderHelper.add(Point.of(x, y));
			return this;
		}

		public Builder add(Point p) {
			builderHelper.add(p);
			return this;
		}

		public Polygon build() {
			final Segment[] data = IntStream.of(0, builderHelper.size())
					.mapToObj(i -> {
						var first = builderHelper.get(Math.floorMod(i, builderHelper.size()));
						var second = builderHelper.get(Math.floorMod(i + 1, builderHelper.size()));
						return new SegmentImpl(first, second);
					})
					.toArray(Segment[]::new);
			return new Polygon(data);
		}

	}


}
