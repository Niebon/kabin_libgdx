package dev.kabin.util.geometry.polygon;

import dev.kabin.util.Functions;

import java.util.Comparator;
import java.util.List;

public class GrahamSmithAlgorithm {

	public static Polygon of(List<Point> points) {
		final Point p = points.stream()
				.min(Functions.dictionaryOrder(Comparator.comparing(Point::y), Comparator.comparing(Point::x)))
				.orElseThrow();
		final Polygon.Builder builder = Polygon.builder().add(p);

		final var pointOther = points.stream()
				.filter(pOther -> !p.equals(pOther))
				.max(Comparator.comparingInt(pOther -> angleScore(p, pOther)))
				.orElseThrow();

		return builder.build();
	}

	private static int angleScore(Point p, Point pointOther) {
		return 0;
	}
}
