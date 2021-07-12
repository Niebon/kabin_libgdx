package dev.kabin.util.geometry;

import dev.kabin.util.geometry.points.PointFloat;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface Triangle {

    static TriangleImmutable immutable(float x1, float y1,
                                       float x2, float y2,
                                       float x3, float y3) {
        var p1 = PointFloat.immutable(x1, y1);
        var p2 = PointFloat.immutable(x2, y2);
        var p3 = PointFloat.immutable(x3, y3);

        var e1 = Edge.immutable(p1, p2);
        var e2 = Edge.immutable(p2, p3);
        var e3 = Edge.immutable(p3, p1);

        return immutable(e1, e2, e3);
    }

    @NotNull
    private static TriangleImmutable immutable(EdgeImmutable e1, EdgeImmutable e2, EdgeImmutable e3) {
        return new TriangleImmutable(e1, e2, e3);
    }

    static TriangleModifiable modifiable(float x1, float y1,
                                         float x2, float y2,
                                         float x3, float y3) {
        var p1 = PointFloat.modifiable(x1, y1);
        var p2 = PointFloat.modifiable(x2, y2);
        var p3 = PointFloat.modifiable(x3, y3);

        var e1 = Edge.modifiable(p1, p2);
        var e2 = Edge.modifiable(p2, p3);
        var e3 = Edge.modifiable(p3, p1);

        return modifiable(e1, e2, e3);
    }

    @NotNull
    static TriangleModifiable modifiable(EdgeModifiable e1, EdgeModifiable e2, EdgeModifiable e3) {
        return new TriangleModifiable(e1, e2, e3);
    }

    default void forEachVertex(Consumer<PointFloat> edgeAction) {
        edgeAction.accept(e1().start());
        edgeAction.accept(e1().end());
        edgeAction.accept(e2().start());
        edgeAction.accept(e2().end());
        edgeAction.accept(e3().start());
        edgeAction.accept(e3().end());
    }

    default void forEachEdge(Consumer<Edge> edgeAction) {
        edgeAction.accept(e1());
        edgeAction.accept(e2());
        edgeAction.accept(e3());
    }

    default PointFloat p1() {
        return e1().start();
    }

    default PointFloat p2() {
        return e2().start();
    }

    default PointFloat p3() {
        return e3().start();
    }

    Edge e1();

    Edge e2();

    Edge e3();

    /**
     * Check if the given triangle contains the given edge.
     *
     * @param edge an edge.
     * @return true if this triangle contains it - false otherwise.
     */
    default boolean hasEdge(Edge edge) {
        return e1().equals(edge) || e2().equals(edge) || e3().equals(edge);
    }

    /**
     * Check if the given triangle contains the given vertex.
     *
     * @param vertex a vertex.
     * @return true if this triangle contains it - false otherwise.
     */
    default boolean hasVertex(FloatCoordinates vertex) {
        return e1().start().equals(vertex) || e1().end().equals(vertex) ||
                e2().start().equals(vertex) || e2().end().equals(vertex) ||
                e3().start().equals(vertex) || e3().end().equals(vertex);
    }

    /**
     * Check if a given point is contained in this 2-simplex.
     *
     * @param x the horizontal coordinate.
     * @param y the vertical coordinate.
     * @return true if the point is contained, and false otherwise (up to numerical error).
     * @implNote An edge e of this triangle together with (x,y) determines another triangle.
     * The algorithm checks if the area of this triangle equals that of the sum of the area of the three triangles obtained
     * from (x,y) and the edges e, of this triangle. (The sense of equality used is up to a numerical error of {@code 10e-6}.
     */
    default boolean contains(float x, float y) {
        float a1 = areaOfTriangleSpannedBy(x, y, p2().x(), p2().y(), p3().x(), p3().y());
        float a2 = areaOfTriangleSpannedBy(p1().x(), p1().y(), x, y, p3().x(), p3().y());
        float a3 = areaOfTriangleSpannedBy(p1().x(), p1().y(), p2().x(), p2().y(), x, y);
        return Math.abs(a1 + a2 + a3 - area()) < 10e-6;
    }

    default boolean contains(FloatCoordinates coordinates) {
        return contains(coordinates.x(), coordinates.y());
    }

    default float area() {
        return areaOfTriangleSpannedBy(p1().x(), p1().y(), p2().x(), p2().y(), p3().x(), p3().y());
    }

    // A helper method for calculating the area of a triangle.
    private float areaOfTriangleSpannedBy(float x1, float y1, float x2, float y2, float x3, float y3) {
        return Math.abs(0.5f * (x1 * (y2 - y3) + x2 * (y3 - y1) + x3 * (y1 - y2)));
    }

    /**
     * @param other the other 2-simplex.
     * @return {@code true} if {@code this} intersects the other simplex, otherwise {@code false}.
     */
    default boolean intersects(Triangle other) {
        return other.contains(p1()) || other.contains(p2()) || other.contains(p3())
                || contains(other.p1()) || contains(other.p2()) || contains(other.p3())
                || e1().intersects(other.e1()) || e1().intersects(other.e2()) || e1().intersects(other.e3())
                || e2().intersects(other.e1()) || e2().intersects(other.e2()) || e2().intersects(other.e3())
                || e3().intersects(other.e1()) || e3().intersects(other.e2()) || e3().intersects(other.e3());
    }

    /**
     * @return a string of the form [x1,y1,x2,y2,x3,y2].
     */
    default String prettyPrint() {
        return "[" + Stream.of(e1(), e2(), e3())
                .mapMulti((e, c) -> {
                    c.accept(e.startX());
                    c.accept(e.startY());
                })
                .map(String::valueOf)
                .collect(Collectors.joining(",")) + "]";
    }

    default List<PointFloat> boundary() {
        return Stream.of(e1(), e2(), e3())
                .map(Edge::start)
                .toList();
    }

}
