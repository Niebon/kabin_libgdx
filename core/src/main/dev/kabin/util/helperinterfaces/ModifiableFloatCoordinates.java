package dev.kabin.util.helperinterfaces;

/**
 * An interface that provides standard methods for modifiable coordinates.
 */
public interface ModifiableFloatCoordinates extends FloatCoordinates {

	/**
	 * Modify the horizontal component.
	 *
	 * @param x the new horizontal component.
	 */
	void setX(float x);

	/**
	 * Modify the vertical component.
	 *
	 * @param y the new vertical component.
	 */
	void setY(float y);

	/**
	 * Modify horizontal and vertical components.
	 *
	 * @param x the horizontal coordinate.
	 * @param y the vertical coordinate.
	 */
	default void setPos(float x, float y) {
		setX(x);
		setY(y);
	}

	/**
	 * Translate by the given delta.
	 *
	 * @param deltaX the horizontal translation.
	 * @param deltaY the vertical translation.
	 */
	default void translate(float deltaX, float deltaY) {
		setPos(x() + deltaX, y() + deltaY);
	}


	/**
	 * Rotates these coordinates about the given pivot point.
	 *
	 * @param pivotX   x coordinate of a pivot point.
	 * @param pivotY   y coordinate of a pivot point.
	 * @param angleRad the angle (in radians) that these coordinates are to be rotated by.
	 */
	default void rotate(float pivotX, float pivotY, double angleRad) {
		setPos(x() - pivotX, y() - pivotY);
		final double cs = Math.cos(angleRad);
		final double sn = Math.sin(angleRad);
		// Apply rotation matrix.
		setPos((float) (x() * cs - y() * sn), (float) (x() * sn + y() * cs));
		setPos(x() + pivotX, y() + pivotY);
	}

	/**
	 * Rotates these coordinates about the origin.
	 *
	 * @param angleRad the angle (in radians) that these coordinates are to be rotated by.
	 */
	default void rotate(double angleRad) {
		rotate(0, 0, angleRad);
	}

}
