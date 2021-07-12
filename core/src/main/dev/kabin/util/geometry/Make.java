package dev.kabin.util.geometry;

import dev.kabin.util.geometry.points.PointFloat;

class Make {

    static PointFloat intersection(Line l1, Line l2) {
        float a = l1.slope();
        float b = l2.slope();
        float c = l1.intercept();
        float d = l2.intercept();
        return PointFloat.immutable(
                (d - c) / (a - b),
                a * (d - c) / (a - b) + c
        );
    }

    static PointFloat midPoint(Edge edge) {
        float x = 0.5f * (edge.startX() + edge.endX());
        float y = 0.5f * (edge.startY() + edge.endY());
        return PointFloat.immutable(x, y);
    }

    public static Circle circumCircleOf(Triangle t) {
        float a = t.e1().length();
        float b = t.e2().length();
        float c = t.e3().length();
        float s = (a + b + c) / 2;
        float r = (float) (a * b * c / (4 * Math.sqrt(s * (s - a) * (s - b) * (s - c))));

        var dir1 = t.e1().direction();
        var dir2 = t.e2().direction();
        var normal1 = dir1.map((x, y) -> -y, (x, y) -> x);
        var normal2 = dir2.map((x, y) -> -y, (x, y) -> x);
        var midPoint1 = midPoint(t.e1());
        var midPoint2 = midPoint(t.e2());
        var line1 = Line.of(midPoint1, normal1);
        var line2 = Line.of(midPoint2, normal2);

        var circumCenter = intersection(line1, line2);

        return new CircleImmutable(circumCenter.x(), circumCenter.y(), r);
    }
}
