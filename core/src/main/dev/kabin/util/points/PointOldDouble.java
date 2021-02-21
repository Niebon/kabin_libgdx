package dev.kabin.util.points;

import dev.kabin.util.HashCodeUtil;
import dev.kabin.util.functioninterfaces.DoubleToDoubleFunction;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Wrapper class for a pair of doubles.
 */
@Deprecated(forRemoval = true)
public class PointOldDouble implements PointOld<Double> {
    public double x, y;

    public PointOldDouble(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public PointOldDouble(@NotNull PointOldDouble p) {
        x = p.x;
        y = p.y;
    }

    public boolean equals(Object p) {
        if (p instanceof PointOldDouble) {
            PointOldDouble other = (PointOldDouble) p;
            return (x == other.x && y == other.y);
        } else return false;
    }


    @Override
    public Double getX() {
        return x;
    }

    public PointOldDouble setX(double x) {
        this.x = x;
        return this;
    }

    @Override
    public Double getY() {
        return y;
    }

    public PointOldDouble setY(double y) {
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
    public PointOld<Double> setX(@NotNull Double x) {
        this.x = x;
        return this;
    }

    @Override
    public PointOld<Double> setY(@NotNull Double y) {
        this.y = y;
        return this;
    }

    public PointOldDouble transform(@NotNull DoubleToDoubleFunction fx, @NotNull DoubleToDoubleFunction fy) {
        x = fx.eval(x);
        y = fy.eval(y);
        return this;
    }

    @Override
    public PointOldDouble rotate(double angleRadians) {
        final double cs = Math.cos(angleRadians), sn = Math.sin(angleRadians);
        x = x * cs - y * sn;
        y = x * sn + y * cs;
        return this;
    }

    public PointOldDouble scaleThis(double scalar) {
        x = x * scalar;
        y = y * scalar;
        return this;
    }

    public PointOldDouble scaleNew(double scalar) {
        return PointOld.of(x,y).scaleThis(scalar);
    }


    public ModifiablePointInt toPointInt() {
        return new ModifiablePointInt((int) Math.round(x), (int) Math.round(y));
    }

    @Override
    public int hashCode() {
        return HashCodeUtil.hashCode(Double.hashCode(x), Double.hashCode(y));
    }

    @Override
    public String toString() {
        return "PointDouble{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }

    @Contract("->new")
    public ModifiablePointInt round(){
        return PointOld.of((int) Math.round(x), (int) Math.round(y));
    }

    @Contract("_ -> this")
    public PointOldDouble translate(@NotNull PointOld<Double> amount){
        x = x + amount.getX();
        y = y + amount.getY();
        return this;
    }
}
