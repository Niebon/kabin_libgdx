package dev.kabin.util.geometry;

import dev.kabin.util.geometry.points.PointFloat;

final class Make {

    private Make() {
    }

    static PointFloat directionOf(Edge segment) {
        return PointFloat.immutable(segment.endX() - segment.startX(), segment.endY() - segment.startY());
    }


    static PointFloat midPoint(Edge edge) {
        float x = 0.5f * (edge.startX() + edge.endX());
        float y = 0.5f * (edge.startY() + edge.endY());
        return PointFloat.immutable(x, y);
    }

    public static Circle circumCircleOf(Triangle t) {
        // TODO: unit test this:
        float a = t.e1().length();
        float b = t.e2().length();
        float c = t.e3().length();
        float s = (a + b + c) / 2;
        float r = (float) (a * b * c / (4 * Math.sqrt(s * (s - a) * (s - b) * (s - c))));

        var dir1 = Make.directionOf(t.e1());
        var dir2 = Make.directionOf(t.e2());
        var normal1 = dir1.map((x, y) -> -y, (x, y) -> x);
        var normal2 = dir2.map((x, y) -> -y, (x, y) -> x);
        var midPoint1 = midPoint(t.e1());
        var midPoint2 = midPoint(t.e2());

        float x1 = midPoint1.x();
        float y1 = midPoint1.y();
        float x2 = x1 + normal1.x();
        float y2 = y1 + normal1.y();
        float x3 = midPoint2.x();
        float y3 = midPoint2.y();
        float x4 = x3 + normal2.x();
        float y4 = y3 + normal2.y();


        float d = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);


        var circumCenter = PointFloat.immutable(
                (x1 * y2 - y1 * x2) * (x3 - x4) - (x1 - x2) * (x3 * y4 - y3 * x4) / d,
                (x1 * y2 - y1 * x2) * (y3 - y4) - (y1 - y2) * (x3 * y4 - y3 * x4) / d
        );

        return new CircleImmutable(circumCenter.x(), circumCenter.y(), r);
    }
}
