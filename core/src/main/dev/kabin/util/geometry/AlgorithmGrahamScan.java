package dev.kabin.util.geometry;

import dev.kabin.util.Functions;
import dev.kabin.util.geometry.points.PointFloat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.NoSuchElementException;

public class AlgorithmGrahamScan {

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

        final PointFloat p0 = points.stream()
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
            while (stack.size() > 1 && Calculate.turnSign(stack.peekNextToTop(), stack.peek(), point) <= 0) {
                stack.pop();
            }
            stack.add(point);
        }

        return Polygon.builder().addAll(stack).buildImmutable();
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
