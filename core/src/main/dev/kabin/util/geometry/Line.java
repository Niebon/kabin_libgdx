package dev.kabin.util.geometry;

public record Line(float slope, float intercept) {

    static Line of(FloatCoordinates p, FloatCoordinates dir) {
        float slope = dir.y() / dir.x();
        float intercept = p.y() - slope * p.x();
        return new Line(slope, intercept);
    }

}
