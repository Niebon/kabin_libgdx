package dev.kabin.util.geometry;

import dev.kabin.util.geometry.points.ImmutablePointFloat;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Arrays;
import java.util.List;

public final class ImmutablePolygon implements Polygon {

	private final SegmentImmutable[] data;

	ImmutablePolygon(SegmentImmutable[] data) {
		this.data = data;
	}

	@Override
	@UnmodifiableView
	public List<ImmutablePointFloat> boundary() {
		return Arrays.stream(data).map(Segment::start).map(ImmutablePointFloat.class::cast).toList();
	}

	@Override
	public String toString() {
		return "Polygon{" +
				"data=" + Arrays.toString(data) +
				'}';
	}


}
