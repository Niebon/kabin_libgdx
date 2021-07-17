package dev.kabin.util.geometry;

import dev.kabin.util.HashCodeUtil;

import java.util.Objects;

public record TriangleImmutable(EdgeImmutable e1, EdgeImmutable e2, EdgeImmutable e3) implements Triangle {

    @Override
    public String toString() {
        return prettyPrint();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Triangle that)) return false;
        return Objects.equals(e1, that.e1()) && Objects.equals(e2, that.e2()) && Objects.equals(e3, that.e3());
    }

    @Override
    public int hashCode() {
        return HashCodeUtil.hashCode(e1.hashCode(), e2.hashCode(), e3.hashCode());
    }

}
