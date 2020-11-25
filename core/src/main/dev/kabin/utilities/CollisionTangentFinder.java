package dev.kabin.utilities;

import dev.kabin.GlobalData;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class CollisionTangentFinder {

    private static final int RADIUS = 5;
    private static final int STEPS = 3;


    private static final Map<Double, int[]> angleToCoordX = new HashMap<>();
    private static final Map<Double, int[]> angleToCoordY = new HashMap<>();

    private static int[] calculateCoordinateX(double angle) {
        return IntStream.range(0, RADIUS).map(radius -> (int) Math.round(radius * Math.cos(Math.toRadians(angle)))).toArray();
    }

    private static int[] calculateCoordinateY(double angle) {
        return IntStream.range(0, RADIUS).map(radius -> (int) Math.round(radius * Math.sin(Math.toRadians(angle)))).toArray();
    }

    private static boolean checkBeamHitsCollision(int[] coordX, int[] coordY, int x, int y) {
        return GlobalData.getRootComponent().isCollisionAt(x + coordX[RADIUS - 1], y + coordY[RADIUS - 1]);
    }

    /**
     * Bisection method to find the best slope estimate.
     */
    public static double calculateCollisionSlope(int x, int y, Direction direction) {

        double bestEstimate = (direction == Direction.LEFT) ? 180 : 0;
        double deg1, deg2, deg3;
        double epsilon = 90;
        int step = 1;
        while (step <= STEPS) {
            deg1 = bestEstimate - epsilon;
            deg2 = bestEstimate;
            deg3 = bestEstimate + epsilon;

            boolean res1 = checkBeamHitsCollision(
                    angleToCoordX.computeIfAbsent(deg1, CollisionTangentFinder::calculateCoordinateX),
                    angleToCoordY.computeIfAbsent(deg1, CollisionTangentFinder::calculateCoordinateY),
                    x, y);

            boolean res2 = checkBeamHitsCollision(
                    angleToCoordX.computeIfAbsent(deg2, CollisionTangentFinder::calculateCoordinateX),
                    angleToCoordY.computeIfAbsent(deg2, CollisionTangentFinder::calculateCoordinateY),
                    x, y);

            boolean res3 = checkBeamHitsCollision(
                    angleToCoordX.computeIfAbsent(deg3, CollisionTangentFinder::calculateCoordinateX),
                    angleToCoordY.computeIfAbsent(deg3, CollisionTangentFinder::calculateCoordinateY),
                    x, y);

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


            step++;
        }

        return bestEstimate;
    }

}
