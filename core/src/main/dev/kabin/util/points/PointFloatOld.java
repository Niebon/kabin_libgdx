package dev.kabin.util.points;

import dev.kabin.util.HashCodeUtil;
import dev.kabin.util.fp.FloatUnaryOperation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@Deprecated(forRemoval = true)
public class PointFloatOld implements PointOld<Float> {

    public float x, y;

    public PointFloatOld(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public PointFloatOld(@NotNull PointFloatOld p) {
        x = p.x;
        y = p.y;
    }

    public boolean equals(Object p) {
        if (p instanceof PointFloatOld other) {
            return (x == other.x && y == other.y);
        } else return false;
    }


    @Override
    public Float getX() {
        return x;
    }

    public PointFloatOld setX(float x) {
        this.x = x;
        return this;
    }

    @Override
    public Float getY() {
        return y;
    }

    public PointFloatOld setY(float y) {
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
    public PointOld<Float> setX(@NotNull Float x) {
        this.x = x;
        return this;
    }

    @Override
    public PointOld<Float> setY(@NotNull Float y) {
        this.y = y;
        return this;
    }

    public PointFloatOld transform(@NotNull FloatUnaryOperation fx, @NotNull FloatUnaryOperation fy) {
        x = fx.eval(x);
        y = fy.eval(y);
        return this;
    }

    @Override
    public PointFloatOld rotate(double angleRadians) {
        final double cs = Math.cos(angleRadians), sn = Math.sin(angleRadians);
        x = (float) (x * cs - y * sn);
        y = (float) (x * sn + y * cs);
        return this;
    }

    public PointFloatOld scaleThis(float scalar) {
        x = x * scalar;
        y = y * scalar;
        return this;
    }

    public PointFloatOld scaleNew(float scalar) {
        return PointOld.of(x, y).scaleThis(scalar);
    }


    public ModifiablePointInt toPointInt() {
        return new ModifiablePointInt(Math.round(x), Math.round(y));
    }

    @Override
    public int hashCode() {
        return HashCodeUtil.hashCode(Float.hashCode(x), Float.hashCode(y));
    }

    @Override
    public String toString() {
        return "PointFloat{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }

    @Contract("_ -> this")
    public PointFloatOld translate(@NotNull PointOld<Float> amount) {
        x = x + amount.getX();
        y = y + amount.getY();
        return this;
    }
}
