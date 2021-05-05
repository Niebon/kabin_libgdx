package dev.kabin.util;

import dev.kabin.util.lambdas.BiIntPredicate;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class TangentFinder {

    private static final int RADIUS = 5;

    private static final Map<Double, int[]> angleToCoordX = new HashMap<>();
    private static final Map<Double, int[]> angleToCoordY = new HashMap<>();
    private static int xLast;
    private static int yLast;

    private static int[] calculateCoordinateX(double angle) {
        return IntStream.range(0, RADIUS).map(radius -> (int) Math.round(radius * Math.cos(Math.toRadians(angle)))).toArray();
    }

    private static int[] calculateCoordinateY(double angle) {
        return IntStream.range(0, RADIUS).map(radius -> (int) Math.round(radius * Math.sin(Math.toRadians(angle)))).toArray();
    }

    private static boolean checkBeamHitsCollision(int[] coordX,
                                                  int[] coordY,
                                                  int x,
                                                  int y,
                                                  BiIntPredicate isCollisionAt) {
        return isCollisionAt.test(x + coordX[RADIUS - 1], y + coordY[RADIUS - 1]);
    }



    /**
     * Bisection method to find the best slope estimate.
     */
    public static double slope(int x, int y, Direction direction, BiIntPredicate isCollisionAt) {
        if (x != xLast || y != yLast) {
            xLast = x;
            yLast = y;
        }

        double bestEstimate = (direction == Direction.LEFT) ? 180 : 0;
        double deg1, deg2, deg3;
        double epsilon = 90;
        while (epsilon >= 30) {
            deg3 = bestEstimate - epsilon; // lower
            deg2 = bestEstimate;
            deg1 = bestEstimate + epsilon; // higher

            boolean res1 = checkBeamHitsCollision(
                    angleToCoordX.computeIfAbsent(deg1, TangentFinder::calculateCoordinateX),
                    angleToCoordY.computeIfAbsent(deg1, TangentFinder::calculateCoordinateY),
                    x, y, isCollisionAt);

            boolean res2 = checkBeamHitsCollision(
                    angleToCoordX.computeIfAbsent(deg2, TangentFinder::calculateCoordinateX),
                    angleToCoordY.computeIfAbsent(deg2, TangentFinder::calculateCoordinateY),
                    x, y, isCollisionAt);

            boolean res3 = checkBeamHitsCollision(
                    angleToCoordX.computeIfAbsent(deg3, TangentFinder::calculateCoordinateX),
                    angleToCoordY.computeIfAbsent(deg3, TangentFinder::calculateCoordinateY),
                    x, y, isCollisionAt);

            if (direction == Direction.RIGHT) {
                if (!res1 && res2) {
                    bestEstimate = (deg1 + deg2) / 2;
                } else if (!res2 && res3) {
                    bestEstimate = (deg2 + deg3) / 2;
                }
            } else if (direction == Direction.LEFT) {
                if (!res3 && res2) {
                    bestEstimate = (deg3 + deg2) / 2;
                } else if (!res2 && res1) {
                    bestEstimate = (deg2 + deg1) / 2;
                }
            }


            epsilon = epsilon * 0.5;

        }

        return bestEstimate;
    }

}
