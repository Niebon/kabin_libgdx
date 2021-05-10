package dev.kabin.util.pathfinding;

import dev.kabin.util.Direction;
import dev.kabin.util.IndexedRect;
import dev.kabin.util.graph.Node;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public record PathFinder(PathData pathData, HeuristicScorer heuristicScorer) {

    private static final int MAX_RECURSION_PATH_SIZE = 10;

    private void calculatePathsToDestinationAStar(List<CheckPoint> currentPath,
                                                  int destination,
                                                  @NotNull List<List<CheckPoint>> finalPathsToDestination) {


        if (finalPathsToDestination.size() > 0) return;
        if (currentPath.size() > MAX_RECURSION_PATH_SIZE) return;

        final int indexPrev = currentPath.get(currentPath.size() - 1).index();
        final List<IndexedRect> pathSegmentNeighborhoods = pathData.pathSegments().andThen(Node::data);
        final Node<IndexedRect> prevPathComponent = pathData.pathSegments().get(indexPrev);

        var arrowsSorted = prevPathComponent
                .children()
                .sortBy(Comparator.comparingDouble(n -> heuristicScorer.heuristicScore(n.data().rect(), prevPathComponent.data().rect(), pathData().pathSegments().get(destination).data().rect())));

        //noinspection ForLoopReplaceableByForEach
        for (int i = 0, n = arrowsSorted.size(); i < n; i++) {
            if (finalPathsToDestination.size() > 0) return;
            final Node<IndexedRect> arrow = arrowsSorted.get(i);
            final int nextIndex = arrow.data().index();
            if (currentPath.stream().noneMatch(e -> e.index() == nextIndex)) {
                final List<CheckPoint> newPath = new ArrayList<>(currentPath);
                newPath.add(new CheckPoint(nextIndex, Direction.NONE, false));
                if (nextIndex == destination) {
                    finalPathsToDestination.add(newPath);
                } else {
                    calculatePathsToDestinationAStar(newPath, destination, finalPathsToDestination);
                }
            }

        }
    }

}
