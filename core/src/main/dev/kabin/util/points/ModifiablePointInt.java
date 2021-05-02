package dev.kabin.util.points;

import dev.kabin.util.HashCodeUtil;
import dev.kabin.util.fp.IntToIntFunction;
import org.jetbrains.annotations.NotNull;

/**
 * Wrapper class for a pair of ints.
 */
public final class ModifiablePointInt implements PointInt {

	private int x, y;

	public ModifiablePointInt(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public ModifiablePointInt(@NotNull PointInt p) {
		x = p.x();
		y = p.y();
	}

	public ModifiablePointInt setX(int x) {
		this.x = x;
		return this;
	}

	public ModifiablePointInt setY(int y) {
		this.y = y;
		return this;
	}

	@Override
	public int x() {
		return x;
	}

	@Override
	public int y() {
		return y;
	}


	public ModifiablePointInt transform(@NotNull IntToIntFunction fx, @NotNull IntToIntFunction fy) {
		x = fx.apply(x);
		y = fy.apply(y);
		return this;
	}

	public ModifiablePointInt rotate(double angleRadians) {
		final double cs = Math.cos(angleRadians), sn = Math.sin(angleRadians);
		x = (int) Math.round(x * cs - y * sn);
		y = (int) Math.round(x * sn + y * cs);
		return this;
	}


	@Override
	final public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof PointInt that)) return false;
		return x == that.x() &&
				y == that.y();
	}

	@Override
	final public int hashCode() {
		return HashCodeUtil.hashCode(x, y);
	}

	@Override
	public String toString() {
		return "PointInt{" +
				"x=" + x +
				", y=" + y +
				'}';
	}

	public ModifiablePointInt translate(@NotNull ModifiablePointInt amount) {
		x = x + amount.x();
		y = y + amount.y();
		return this;
	}


}
