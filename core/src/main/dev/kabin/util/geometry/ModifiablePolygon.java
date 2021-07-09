package dev.kabin.util.geometry;

import dev.kabin.util.geometry.points.ImmutablePointFloat;
import dev.kabin.util.geometry.points.PointFloat;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Arrays;
import java.util.List;

public final class ModifiablePolygon implements Polygon {

	private final SegmentModifiable[] data;

	ModifiablePolygon(SegmentModifiable[] data) {
		this.data = data;
	}

	@Override
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


}
