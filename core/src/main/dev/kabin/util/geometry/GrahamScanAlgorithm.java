package dev.kabin.util.geometry;

import dev.kabin.util.Functions;
import dev.kabin.util.geometry.points.PointFloat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.NoSuchElementException;

public class GrahamScanAlgorithm {

    /**
     * Performs a Graham scan algorithm on the given collection of points.
     *
     * @param candidatePoints a set of points. Expected to contain at least 3 non-equal point instances.
     * @return a convex Polygon that represents the shape of the given point collection.
     */
    // https://edutechlearners.com/download/Introduction_to_algorithms-3rd%20Edition.pdf
    // See Graham’s scan
    public static Polygon on(Collection<PointFloat> candidatePoints) {

        final var points = new ArrayList<>(candidatePoints);
        final var stack = new ArrayStack<PointFloat>(candidatePoints.size());

        PointFloat p0 = points.stream()
                .min(Comparator.comparing(PointFloat::y).thenComparing(PointFloat::x))
                .orElseThrow();

        points.remove(p0);
        // First compare angle.
        // If angles do not determine a winner, find who's farthest away from p0.
        points.sort(Comparator.<PointFloat>comparingDouble(p -> Functions.findAngleRad(p.x() - p0.x(), p.y() - p0.y()))
                .thenComparing(Comparator.<PointFloat>comparingDouble(p1 -> Functions.distance(p1.x(), p1.y(), p0.x(), p0.y())).reversed()));

        stack.add(p0);
        stack.add(points.get(0));
        stack.add(points.get(1));

        for (var point : points.subList(2, points.size())) {
            // While angle formed by points [stack.peekNextToTop(), stack.peek(), point] makes a non-left turn, pop the stack:
            while (stack.size() > 1 && turnSign(stack.peekNextToTop(), stack.peek(), point) <= 0) {
                stack.pop();
            }
            stack.add(point);
        }

        return Polygon.builder().addAll(stack).buildImmutable();
    }

    private static PointFloat direction(PointFloat p1, PointFloat p2) {
        return PointFloat.immutable(p2.x() - p1.x(), p2.y() - p1.y());
    }

    /**
     * Calculates the cross product p1p2 x p2p3.
     *
     * @param p1 the first point.
     * @param p2 the second point.
     * @param p3 the third point.
     * @return the sign of the direction of the turn p1 -> p2 -> p3. If the direction goes to the left, then the sign is positive.
     * Otherwise it is negative.
     */
    private static float turnSign(PointFloat p1, PointFloat p2, PointFloat p3) {
        var p1p2 = direction(p1, p2);
        var p2p3 = direction(p2, p3);
        return p1p2.cross(p2p3);
    }

    // A little helper class to model a stack interface.
    final static class ArrayStack<T> extends ArrayList<T> {

        public ArrayStack(int initialCapacity) {
            super(initialCapacity);
        }

        public T peek() throws NoSuchElementException {
            return get(size() - 1);
        }

        public T peekNextToTop() throws NoSuchElementException {
            return get(size() - 2);
        }

        @SuppressWarnings("UnusedReturnValue")
        public T pop() {
            return remove(size() - 1);
        }
    }

}
