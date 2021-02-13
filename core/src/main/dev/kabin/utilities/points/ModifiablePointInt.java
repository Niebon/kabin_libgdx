package dev.kabin.utilities.points;

import dev.kabin.utilities.HashCodeUtil;
import dev.kabin.utilities.functioninterfaces.IntToIntFunction;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Wrapper class for a pair of ints.
 */
public class ModifiablePointInt implements PointInt {

	public int x, y;

	public ModifiablePointInt(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public ModifiablePointInt(@NotNull PointInt p) {
		x = p.getX();
		y = p.getY();
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
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}

	public int x() {
		return x;
	}

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
		if (!(o instanceof PointInt)) return false;
		PointInt that = (PointInt) o;
		return x == that.getX() &&
				y == that.getY();
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

	public PointDouble toPointDouble(double scale) {
		return Point.of(x * scale, y * scale);
	}

	public PointDouble toPointDouble() {
		//noinspection RedundantCast: I'd rather have the code look symmetric and look at a silly warning to remove one of the casts.
		return Point.of((double) x, (double) y);
	}

	public ModifiablePointInt translate(@NotNull ModifiablePointInt amount) {
		x = x + amount.x();
		y = y + amount.y();
		return this;
	}


}