package dev.kabin.util.geometry;

public record Line(float slope, float intercept) {

    static Line of(FloatCoordinates point, FloatCoordinates dir) {
        float slope = dir.y() / dir.x();
        float intercept = point.y() - slope * point.x();
        return new Line(slope, intercept);
    }

}
