package dev.kabin.utilities.points;

import dev.kabin.utilities.functioninterfaces.DoubleToDoubleFunction;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Wrapper class for a pair of doubles.
 */
public class PointDouble implements Point<Double> {
    public double x, y;

    public PointDouble(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public PointDouble(@NotNull PointDouble p) {
        x = p.x;
        y = p.y;
    }

    public boolean equals(Object p) {
        if (p instanceof PointDouble) {
            PointDouble other = (PointDouble) p;
            return (x == other.x && y == other.y);
        } else return false;
    }


    @Override
    public Double getX() {
        return x;
    }

    public PointDouble setX(double x) {
        this.x = x;
        return this;
    }

    @Override
    public Double getY() {
        return y;
    }

    public PointDouble setY(double y) {
        this.y = y;
        return this;
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    @Override
    public Point<Double> setX(@NotNull Double x) {
        this.x = x;
        return this;
    }

    @Override
    public Point<Double> setY(@NotNull Double y) {
        this.y = y;
        return this;
    }

    public PointDouble transform(@NotNull DoubleToDoubleFunction fx, @NotNull DoubleToDoubleFunction fy) {
        x = fx.eval(x);
        y = fy.eval(y);
        return this;
    }

    @Override
    public PointDouble rotate(double angleRadians) {
        final double cs = Math.cos(angleRadians), sn = Math.sin(angleRadians);
        x = x * cs - y * sn;
        y = x * sn + y * cs;
        return this;
    }

    public PointDouble scaleThis(double scalar) {
        x = x * scalar;
        y = y * scalar;
        return this;
    }

    public PointDouble scaleNew(double scalar) {
        return Point.of(x,y).scaleThis(scalar);
    }


    public PointInt toPointInt() {
        return new PointInt((int) Math.round(x), (int) Math.round(y));
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "PointDouble{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }

    @Contract("->new")
    public PointInt round(){
        return Point.of((int) Math.round(x), (int) Math.round(y));
    }

    @Contract("_ -> this")
    public PointDouble translate(@NotNull Point<Double> amount){
        x = x + amount.getX();
        y = y + amount.getY();
        return this;
    }
}
