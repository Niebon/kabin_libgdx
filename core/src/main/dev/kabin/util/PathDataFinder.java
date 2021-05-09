package dev.kabin.util;

import dev.kabin.entities.Entity;
import dev.kabin.util.points.PointInt;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PathDataFinder {


	public void gatherPathData(List<Entity<?, ?, ?>> entitiesInCameraView) {
		final List<PointInt> collisionData = entitiesInCameraView
				.stream()
				.map(Entity::collisionRelativeToWorld)
				.map(l -> l.sortBy(Comparator.comparingInt(PointInt::x)))
				.<PointInt>mapMulti(Iterable::forEach)
				.filter(Objects::nonNull)
				.collect(Collectors.toCollection(ArrayList::new));
	}

}
