package dev.kabin.util.geometry;

import dev.kabin.util.Pair;
import dev.kabin.util.geometry.points.PointFloat;
import dev.kabin.util.lambdas.Function;
import dev.kabin.util.lambdas.Projection;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AlgorithmChiShape {

    // Efficient generation of simple polygons for characterizing the shape of a set of points in the plane
    // http://geosensor.net/papers/duckham08.PR.pdf
    public static Polygon on(Collection<? extends PointFloat> pointList, float l) {
        var delaunayTriangulation = AlgorithmBowyerWatson.calculateDelaunayTriangulation(pointList);

        // Interpret the triangulation the sense of the reference paper:
        var chiEdgeTriangulation = delaunayTriangulation
                .stream()
                .mapMulti(Triangle::forEachEdge)
                .map(ChiEdge::of)
                //.distinct() Include this if changing to a non-set collection.
                .collect(Collectors.toCollection(HashSet::new));

        var setOfDarts = chiEdgeTriangulation
                .stream()
                .<Dart>mapMulti((e, c) -> {
                    c.accept(e.d1());
                    c.accept(e.d2());
                })
                .collect(Collectors.toCollection(HashSet::new));

        // A dart involution that maps darts to their inverse.
        var dartInvolution = (Function<Dart, Dart>) setOfDarts.stream()
                .map(dart -> {
                            var inverse = setOfDarts.stream().filter(dart::isInverse).findFirst().orElseThrow();
                            return new Pair<>(dart, inverse);
                        }
                ).collect(Collectors.toMap(Pair::left, Pair::right))::get;

        // A dart bijection that maps darts starting a vertex, to the first other such dart
        // found when scanning in a counter clockwise direction.
        var dartBijectionShiftCounterClockwise = (Function<Dart, Dart>) setOfDarts.stream()
                .collect(Collectors.groupingBy(Dart::start))
                .values()
                .stream()
                .<Pair<Dart, Dart>>mapMulti((dartList, c) -> {
                    final List<Dart> dartListSortedByAngle = dartList.stream().sorted(Comparator.comparing(dart -> dart.edge.angleRad())).toList();
                    for (int i = 0, n = dartListSortedByAngle.size(); i < n; i++) {
                        final Dart prev = dartListSortedByAngle.get(Math.floorMod(i, n));
                        final Dart next = dartListSortedByAngle.get(Math.floorMod(i + 1, n));
                        c.accept(new Pair<>(prev, next));
                    }
                })
                .collect(Collectors.toMap(Pair::left, Pair::right))::get;

        var functions = new Functions(dartInvolution, dartBijectionShiftCounterClockwise);

        var boundary = chiEdgeTriangulation
                .stream()
                .filter(functions::eBoundaryFunction)
                .sorted(Comparator.comparingDouble(ChiEdge::length))
                .collect(Collectors.toCollection(ArrayList::new));

        var vBoundaryFunction = (Map<Dart, Boolean>) chiEdgeTriangulation
                .stream()
                .filter(functions::eBoundaryFunction)
                .<Pair<Dart, Boolean>>mapMulti((chiEdge, c) -> {
                    c.accept(new Pair<>(chiEdge.d1(), true));
                    c.accept(new Pair<>(chiEdge.d2(), true));
                })
                .collect(Collectors.toMap(Pair::left, Pair::right, Projection::left, HashMap::new));


        while (!boundary.isEmpty()) {
            var e = boundary.remove(boundary.size() - 1);
            if (e.length() > l && functions.isRegular(vBoundaryFunction::get, e)) {
                chiEdgeTriangulation.remove(e);
                var chiEdge1 = ChiEdge.of(functions.reveal(e.d1()).edge());
                var chiEdge2 = ChiEdge.of(functions.reveal(e.d2()).edge());
                boundary.add(chiEdge1);
                boundary.add(chiEdge2);
                vBoundaryFunction.put(chiEdge1.d1(), true);
            }
        }

        return Polygon.builder().addAll(boundary.stream().mapMulti(ChiEdge::forEach).map(Dart::edge).map(Edge::start).distinct().toList()).buildImmutable();
    }

    private record Functions(Function<Dart, Dart> theta0, Function<Dart, Dart> theta1) {

        boolean eBoundaryFunction(Dart dart1, Dart dart2) {
            return !dart1.equals(theta010101(dart1)) && !dart2.equals(theta010101(dart2));
        }

        Dart theta010101(Dart dart1) {
            return theta1(theta01010(dart1));
        }

        Dart theta01010(Dart dart1) {
            return theta0(theta1(theta0(theta1(theta0(dart1)))));
        }


        Dart theta0(Dart d) {
            return theta0.apply(d);
        }

        Dart theta1(Dart d) {
            return theta1.apply(d);
        }

        Dart reveal(Dart d) {
            return theta010101(d).equals(d) ? theta1(d) : theta01010(d);
        }

        boolean eBoundaryFunction(ChiEdge edge) {
            return eBoundaryFunction(edge.d1(), edge.d2());
        }

        // Algorithm 2.
        boolean isRegular(Predicate<Dart> vBoundaryFunction,
                          ChiEdge e) {
            if (eBoundaryFunction(e)) {
                var arbitraryDart = e.d1();
                var v = theta0(reveal(arbitraryDart));
                return !vBoundaryFunction.test(v);
            }
            return false;
        }
    }

    static record Dart(Edge edge) {

        public PointFloat start() {
            return edge.start();
        }

        public PointFloat end() {
            return edge.end();
        }

        public float startX() {
            return edge.startX();
        }

        public float startY() {
            return edge.startY();
        }

        public float endX() {
            return edge.endX();
        }

        public float endY() {
            return edge.endY();
        }

        boolean isInverse(Dart other) {
            return edge.isInverseTo(other.edge);
        }

        boolean startsIn(FloatCoordinates point) {
            return edge.start().equals(point);
        }

    }


    record ChiEdge(Dart d1, Dart d2) {

        public static ChiEdge of(Edge edge) {
            return new ChiEdge(new Dart(edge), new Dart(Edge.inverse(edge)));
        }

        public double length() {
            return d1.edge.length();
        }

        public void forEach(Consumer<Dart> dartConsumer) {
            dartConsumer.accept(d1);
            dartConsumer.accept(d2);
        }
    }

}
