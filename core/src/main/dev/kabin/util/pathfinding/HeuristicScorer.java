package dev.kabin.util.pathfinding;

import dev.kabin.util.Functions;
import dev.kabin.util.lambdas.BiIntPredicate;
import dev.kabin.util.shapes.primitive.RectInt;
import org.jetbrains.annotations.NotNull;


public record HeuristicScorer(BeingMetadata beingMetadata, PhysicsConstants physicsConstant,
							  BiIntPredicate collisionAt) {

	/**
	 * Heuristic score for next in A*-pathfinder algorithm.
	 * Favors paths such that next is close to the destination unless next is reachable by a jump.
	 *
	 * @param next        (next path segment)
	 * @param prev        (prev path segment)
	 * @param destination (destination)
	 * @return score
	 */
    double score(RectInt next, @NotNull RectInt prev, RectInt destination) {
		return Functions.distance(next.getCenterX(), next.getMaxY(), destination.getCenterX(), destination.getMaxY())
				+ ((next.getMaxY() > prev.getMaxY() &&
				PathDataFinder.existsJumpTrajectory(prev, next, beingMetadata, physicsConstant, collisionAt)) ? 256 : 0);
    }




}
