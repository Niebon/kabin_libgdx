package dev.kabin.geometry.points;

import dev.kabin.utilities.functioninterfaces.FloatToFloatFunction;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class PointFloat implements Point<Float> {

    public float x, y;

    public PointFloat(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public PointFloat(@NotNull PointFloat p) {
        x = p.x;
        y = p.y;
    }

    public boolean equals(Object p) {
        if (p instanceof PointFloat) {
            PointFloat other = (PointFloat) p;
            return (x == other.x && y == other.y);
        } else return false;
    }


    @Override
    public Float getX() {
        return x;
    }

    public PointFloat setX(float x) {
        this.x = x;
        return this;
    }

    @Override
    public Float getY() {
        return y;
    }

    public PointFloat setY(float y) {
        this.y = y;
        return this;
    }

    public float x() {
        return x;
    }

    public float y() {
        return y;
    }

    @Override
    public Point<Float> setX(@NotNull Float x) {
        this.x = x;
        return this;
    }

    @Override
    public Point<Float> setY(@NotNull Float y) {
        this.y = y;
        return this;
    }

    public PointFloat transform(@NotNull FloatToFloatFunction fx, @NotNull FloatToFloatFunction fy) {
        x = fx.apply(x);
        y = fy.apply(y);
        return this;
    }

    @Override
    public PointFloat rotate(double angleRadians) {
        final double cs = Math.cos(angleRadians), sn = Math.sin(angleRadians);
        x = (float) (x * cs - y * sn);
        y = (float) (x * sn + y * cs);
        return this;
    }

    public PointFloat scaleThis(float scalar) {
        x = x * scalar;
        y = y * scalar;
        return this;
    }

    public PointFloat scaleNew(float scalar) {
        return Point.of(x, y).scaleThis(scalar);
    }


    public PointInt toPointInt() {
        return new PointInt(Math.round(x), Math.round(y));
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "PointFloat{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }

    @Contract("_ -> this")
    public PointFloat translate(@NotNull Point<Float> amount) {
        x = x + amount.getX();
        y = y + amount.getY();
        return this;
    }
}