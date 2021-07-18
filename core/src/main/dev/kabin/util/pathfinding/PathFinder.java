package dev.kabin.util.pathfinding;

import dev.kabin.util.Direction;
import dev.kabin.util.collections.LazyList;
import dev.kabin.util.events.Lists;
import dev.kabin.util.graph.Node;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * A path finder uses path data and a heuristic scorer to
 */
public record PathFinder(PathData pathData, HeuristicScorer heuristicScorer) {

	private static final int MAX_RECURSION_PATH_SIZE = 10;


	/**
	 * Recursively finds paths.
	 *
	 * @param currentPath             the current path.
	 * @param destination             the destination.
	 * @param finalPathsToDestination a list of final paths leading to the destination.
	 */
	private void calculatePathsToDestinationAStar(List<CheckPoint> currentPath,
												  int destination,
												  @NotNull List<List<CheckPoint>> finalPathsToDestination) {


		if (finalPathsToDestination.size() > 0) return;
		if (currentPath.size() > MAX_RECURSION_PATH_SIZE) return;

		final int indexPrev = currentPath.get(currentPath.size() - 1).pathIndex();
		final Node<IndexedRect> prevPathComponent = pathData.pathSegments().get(indexPrev);

		final LazyList<Node<IndexedRect>> arrowsSorted = prevPathComponent
				.children()
				.sortBy(Comparator.comparingDouble(n -> heuristicScorer.score(n.obj().rect(),
						prevPathComponent.obj().rect(),
						pathData().pathSegments().get(destination).obj().rect())));

		//noinspection ForLoopReplaceableByForEach
		for (int i = 0, n = arrowsSorted.size(); i < n; i++) {
			if (finalPathsToDestination.size() > 0) return;
			final Node<IndexedRect> arrow = arrowsSorted.get(i);
			final int nextIndex = arrow.obj().index();
			if (currentPath.stream().noneMatch(e -> e.pathIndex() == nextIndex)) {
				final boolean shouldJump = arrow.obj().connectedIndex() != prevPathComponent.obj().connectedIndex();
				final ArrayList<CheckPoint> newPath = Lists.arrayListOf(currentPath, new CheckPoint(nextIndex, Direction.NONE, shouldJump));
				if (nextIndex == destination) {
					finalPathsToDestination.add(newPath);
				} else {
					calculatePathsToDestinationAStar(newPath, destination, finalPathsToDestination);
				}
			}
		}
	}

}
