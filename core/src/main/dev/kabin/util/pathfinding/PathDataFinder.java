package dev.kabin.util.pathfinding;

import dev.kabin.util.Functions;
import dev.kabin.util.Lists;
import dev.kabin.util.collections.IntToIntMap;
import dev.kabin.util.collections.LazyList;
import dev.kabin.util.graph.SimpleNode;
import dev.kabin.util.lambdas.BiIntPredicate;
import dev.kabin.util.points.PointInt;
import dev.kabin.util.shapes.primitive.GrowingRectInt;
import dev.kabin.util.shapes.primitive.RectInt;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

record PathDataFinder(PhysicsConstants physicsConstants, BiIntPredicate collisionAt) {

	private final static float EPSILON = 4;
	private final static int MAX_PATH_SIZE = 16;

	private static boolean touching(RectInt A, RectInt B) {
		var expandedA = new GrowingRectInt(A.getMinX() - 1, A.getMinY() - 1, A.getWidth() + 2, A.getHeight() + 2);
		var expandedB = new GrowingRectInt(B.getMinX() - 1, B.getMinY() - 1, B.getWidth() + 2, B.getHeight() + 2);
		return expandedA.meets(expandedB);
	}

	static boolean existsJumpTrajectory(RectInt from,
										RectInt to,
										NpcMetadata npcMetadata,
										PhysicsConstants physicsConstants,
										BiIntPredicate collisionAt) {

		if (Math.abs(from.getCenterX() - to.getCenterX()) > 256) return false;

		// initial conditions
		float
				x0 = from.getCenterX(),
				y0 = from.getMinY(),
				x1 = to.getCenterX(),
				y1 = to.getMinY(),
				vx = npcMetadata.vx(),
				vy = npcMetadata.vy(),
				heightConstant = npcMetadata.heightConstant(),
				g = physicsConstants.g(),
				meter = physicsConstants.meter(),
				t = -vy / g,                                // time until dy/dt = 0 after jump
				t_eval_at_x1 = Math.abs((x1 - x0) / vx);    // time until point x1 is reached

		if ((y1 - y0) < y0 + vy * t - 0.5 * g * t * t) return false;

		// Calculate jump trajectory (piece-wise if collision).
		// The jump trajectory exists trajectory ef the position evaluated at x1 is sufficiently close to (x1,y1).
		//
		int n = 8; // Sample points
		float dt = t / n; // Time step (seconds)
		float t_eval_at_x1_after_collision = 0; // Placeholder for duration from collision to when x1 is reached
		float x = x0, y = y0;
		boolean collision = false;
		for (int i = 0; i <= n; i++) {
			float t_ = i * dt;
			x = x + vx * dt;
			y = y + (vy - g * t_) * dt;
			if (collisionAt.test(Math.round(x), Math.round(y - heightConstant))) {
				collision = true;
				t_eval_at_x1_after_collision = t_ + Math.abs((x1 - x) / vx);
			}
		}

		final float trajectory_evaluated_at_x1, y0_after_collision = y;
		if (collision)
			trajectory_evaluated_at_x1 = y0_after_collision - 0.5f * g * t_eval_at_x1_after_collision * t_eval_at_x1_after_collision;
		else trajectory_evaluated_at_x1 = y0 + vy * t_eval_at_x1 - 0.5f * g * t_eval_at_x1 * t_eval_at_x1;
		return (Math.abs(trajectory_evaluated_at_x1 - y1) < findDistanceTolerance(vx, meter));
	}

	/**
	 * Rough tests with
	 * <p>
	 * <p> a) walking speed ~ 3 m/s
	 * <p> b) run speed     ~ 8 m/s
	 * <p>(assuming jump vel of ~  m/s)
	 * <p>
	 * yielded the following optimal tolerances:
	 * <p>
	 * <p> a) 4   * EPSILON
	 * <p> b) 0.5 * EPSILON
	 * <p>
	 * This method returns a linear function that fits these data.
	 * The tolerance greatly impacts the number of arrows created, which in turn greatly affects the cost of the
	 * recursive path finder algorithm.
	 * <p>
	 * (Add one extra point and return exponential fit?)
	 */
	private static float findDistanceTolerance(float vx,
											   float meter) {
		final float optimalFactorWalkingSpeed = 4f,
				optimalFactorRunningSpeed = 0.5f,
				walkingSpeedUsedToDetermineOptimalFactor = 3 * meter,
				runningSpeedUsedToDetermineOptimalFactor = 8 * meter,
				a = (optimalFactorRunningSpeed - optimalFactorWalkingSpeed) / (runningSpeedUsedToDetermineOptimalFactor - walkingSpeedUsedToDetermineOptimalFactor),
				b = optimalFactorRunningSpeed - a * runningSpeedUsedToDetermineOptimalFactor;
		return (a * vx + b) * EPSILON;
	}

	private static void addToNewPathSegment(PointInt pointInt,
											ArrayList<ArrayList<PointInt>> pathSegments,
											ArrayList<GrowingRectInt> pathSegmentNeighborhoods,
											ArrayList<ArrayList<GrowingRectInt>> pathSegmentNeighborhoodsConnectedList,
											IntToIntMap pathSegmentIndexToConnectedIndex) {

		var newNeighborhood = new GrowingRectInt(pointInt.x(), pointInt.y(), 0, 0);
		pathSegmentNeighborhoods.add(newNeighborhood);


		pathSegments.add(Lists.arrayListOf(pointInt));


		// Find correct connected index if current segment is connected
		if (pathSegmentNeighborhoodsConnectedList.isEmpty()) {
			pathSegmentNeighborhoodsConnectedList.add(Lists.arrayListOf(newNeighborhood));
			pathSegmentIndexToConnectedIndex.put(pathSegments.size() - 1, 0);
		} else {
			boolean newNeighborhoodMeetsNoOtherNeighborhoods = true;
			int n = pathSegmentNeighborhoodsConnectedList.size();

			outer:
			for (int index = 1; index < n; index++) {
				ArrayList<GrowingRectInt> listOfConnectedPathSegmentNeighborhoods = pathSegmentNeighborhoodsConnectedList.get(index);
				for (RectInt r : listOfConnectedPathSegmentNeighborhoods) {
					if (touching(r, newNeighborhood)) {
						listOfConnectedPathSegmentNeighborhoods.add(newNeighborhood);
						newNeighborhoodMeetsNoOtherNeighborhoods = false;
						pathSegmentIndexToConnectedIndex.put(pathSegments.size() - 1, index);
						break outer;
					}
				}
			}
			if (newNeighborhoodMeetsNoOtherNeighborhoods) {
				pathSegmentNeighborhoodsConnectedList.add(Lists.arrayListOf(newNeighborhood));
				pathSegmentIndexToConnectedIndex.put(pathSegments.size() - 1, pathSegmentNeighborhoodsConnectedList.size() - 1);
			}
		}
	}

	private static boolean addPointToExistingPathSegment(PointInt p,
														 ArrayList<ArrayList<PointInt>> pathSegments,
														 List<GrowingRectInt> pathSegmentNeighborhoods) {
		int size = pathSegments.size();
		boolean added = false;
		for (int i = 0; i < size; i++) {
			final List<PointInt> pathSegment = pathSegments.get(i);
			if (pathSegment.isEmpty() || pathSegment.size() > MAX_PATH_SIZE) continue;
			final PointInt last = pathSegment.get(pathSegment.size() - 1);
			if (Functions.distance(last.x(), last.y(), p.x(), p.y()) < EPSILON) {
				pathSegment.add(p);
				pathSegmentNeighborhoods.get(i).add(p.x(), p.y());
				added = true;
			}
		}
		return added;
	}

	public Function<NpcMetadata, PathData> newPathDataMaker(Stream<LazyList<PointInt>> collisionContributionPerEntity) {

		var indexToPathSegment = new ArrayList<ArrayList<PointInt>>();
		var pathSegmentNeighborhoods = new ArrayList<GrowingRectInt>();
		var pathSegmentNeighborhoodsConnectedList = new ArrayList<ArrayList<GrowingRectInt>>();
		var pathSegmentToConnectedIndex = new IntToIntMap();

		collisionContributionPerEntity
				.map(l -> l.split(Comparator.comparingInt(PointInt::x)))
				.map(lsp -> lsp.andThen(pts -> pts.reduce((p1, p2) -> p1.y() > p2.y() ? p1 : p2)))
				.<PointInt>mapMulti(Iterable::forEach)
				.filter(Objects::nonNull)
				.distinct()
				.sorted(Functions.dictionaryOrder(Comparator.comparingInt(PointInt::x), Comparator.comparingInt(PointInt::y)))
				.forEach(pt -> {
							if (!addPointToExistingPathSegment(pt, indexToPathSegment, pathSegmentNeighborhoods)) {
								addToNewPathSegment(pt, indexToPathSegment, pathSegmentNeighborhoods, pathSegmentNeighborhoodsConnectedList, pathSegmentToConnectedIndex);
							}
						}
				);

		return r -> {
			var pathSegments = IntStream.range(0, indexToPathSegment.size())
					.mapToObj(i -> new IndexedRect(pathSegmentNeighborhoods.get(i), i, pathSegmentToConnectedIndex.get(i)))
					.filter(ir -> Functions.distance(ir.rect().getCenterX(), ir.rect().getCenterY(), r.x(), r.y()) < 512)
					.map(SimpleNode::new)
					.collect(Collectors.toCollection(ArrayList::new));


			var pathData = new PathData(new LazyList<>(pathSegments::get, pathSegments::size));
			generateArrows(pathData, r);
			return pathData;
		};

	}

	private void generateArrows(@NotNull PathData pathData,
								NpcMetadata npc) {
		for (var nbd1 : pathData.pathSegments()) {
			for (var nbd2 : pathData.pathSegments()) {
				if (nbd1 == nbd2) continue;

				if (touching(nbd1.obj().rect(), nbd2.obj().rect())) {
					nbd1.addChild(nbd2);
					nbd2.addChild(nbd1);
				} else if (nbd1.obj().connectedIndex() != nbd2.obj().connectedIndex()
						&& existsJumpTrajectory(nbd1.obj().rect(), nbd2.obj().rect(), npc, physicsConstants, collisionAt))
					nbd1.addChild(nbd2);
			}
		}
	}


}
