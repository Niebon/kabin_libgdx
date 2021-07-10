package dev.kabin.util.geometry.points;

import dev.kabin.util.HashCodeUtil;
import dev.kabin.util.geometry.ModifiableFloatCoordinates;
import dev.kabin.util.lambdas.FloatToFloatFunction;
import org.jetbrains.annotations.NotNull;

/**
 * Wrapper class for a pair of ints.
 */
public final class PointFloatModifiable implements PointFloat, ModifiableFloatCoordinates {

	private float x, y;

	public PointFloatModifiable(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public PointFloatModifiable(@NotNull PointFloat p) {
		x = p.x();
		y = p.y();
	}

	@Override
	public float x() {
		return x;
	}

	@Override
	public float y() {
		return y;
	}


	public PointFloatModifiable transform(@NotNull FloatToFloatFunction fx, @NotNull FloatToFloatFunction fy) {
		x = fx.apply(x);
		y = fy.apply(y);
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

	@Override
	public void setX(float x) {
		this.x = x;
	}

	@Override
	public void setY(float y) {
		this.y = y;
	}

	@SuppressWarnings("MethodDoesntCallSuperMethod")
	@Override
	public PointFloatModifiable clone() {
		return new PointFloatModifiable(x, y);
	}
}
