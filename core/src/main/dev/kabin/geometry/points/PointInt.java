package dev.kabin.geometry.points;

import dev.kabin.geometry.shapes.RectFloat;
import dev.kabin.utilities.functioninterfaces.IntToIntFunction;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Wrapper class for a pair of ints.
 */
public class PointInt implements PrimitivePointInt {

	public int x, y;

	public PointInt(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public PointInt(@NotNull PointInt p) {
		x = p.x;
		y = p.y;
	}

	public boolean equals(Object p) {
		if (p instanceof PointInt) {
			PointInt other = (PointInt) p;
			return (x == other.x && y == other.y);
		} else return false;
	}

	public PointInt setX(int x) {
		this.x = x;
		return this;
	}

	public PointInt setY(int y) {
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


	public PointInt transform(@NotNull IntToIntFunction fx, @NotNull IntToIntFunction fy) {
		x = fx.apply(x);
		y = fy.apply(y);
		return this;
	}

	public PointInt rotate(double angleRadians) {
		final double cs = Math.cos(angleRadians), sn = Math.sin(angleRadians);
		x = (int) Math.round(x * cs - y * sn);
		y = (int) Math.round(x * sn + y * cs);
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, y);
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

	public PointInt translate(@NotNull PointInt amount) {
		x = x + amount.x();
		y = y + amount.y();
		return this;
	}


}
