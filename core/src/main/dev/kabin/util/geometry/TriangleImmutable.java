package dev.kabin.util.geometry;

public record TriangleImmutable(EdgeImmutable e1, EdgeImmutable e2, EdgeImmutable e3) implements Triangle {
    @Override
    public String toString() {
        return prettyPrint();
    }
}
