package dev.kabin.util.geometry.points;

import dev.kabin.util.HashCodeUtil;
import dev.kabin.util.lambdas.FloatToFloatFunction;
import org.jetbrains.annotations.NotNull;

/**
 * Wrapper class for a pair of ints.
 */
public final class ModifiablePointFloat implements PointFloat {

	private float x, y;

	public ModifiablePointFloat(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public ModifiablePointFloat(@NotNull PointFloat p) {
		x = p.x();
		y = p.y();
	}

	public ModifiablePointFloat setX(float x) {
		this.x = x;
		return this;
	}

	public ModifiablePointFloat setY(float y) {
		this.y = y;
		return this;
	}

	@Override
	public float x() {
		return x;
	}

	@Override
	public float y() {
		return y;
	}


	public ModifiablePointFloat transform(@NotNull FloatToFloatFunction fx, @NotNull FloatToFloatFunction fy) {
		x = fx.apply(x);
		y = fy.apply(y);
		return this;
	}

	public ModifiablePointFloat rotate(double angleRadians) {
		final double cs = Math.cos(angleRadians), sn = Math.sin(angleRadians);
		x = (int) Math.round(x * cs - y * sn);
		y = (int) Math.round(x * sn + y * cs);
		return this;
	}


	@Override
	final public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof PointFloat that)) return false;
		return x == that.x() &&
				y == that.y();
	}

	@Override
	final public int hashCode() {
		return HashCodeUtil.hashCode(Float.hashCode(x), Float.hashCode(y));
	}

	@Override
	public String toString() {
		return "PointFloat{" +
				"x=" + x +
				", y=" + y +
				'}';
	}

	public ModifiablePointFloat translate(float x, float y) {
		this.x = this.x + x;
		this.y = this.y + y;
		return this;
	}


}
