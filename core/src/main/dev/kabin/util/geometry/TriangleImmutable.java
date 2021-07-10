package dev.kabin.util.geometry;

public record TriangleImmutable(SegmentImmutable e1, SegmentImmutable e2, SegmentImmutable e3) implements Triangle {
}
