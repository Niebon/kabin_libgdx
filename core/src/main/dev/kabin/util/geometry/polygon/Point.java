package dev.kabin.util.geometry.polygon;

import dev.kabin.util.geometry.primitive.PointImpl;
import dev.kabin.util.helperinterfaces.ModifiableFloatCoordinates;

/**
 * A 0-simplex is a point (x,y) with modifiable {@code float} coordinates.
 */
public interface Point extends ModifiableFloatCoordinates {

	/**
	 * A factory constructor.
	 *
	 * @param x horizontal coordinate.
	 * @param y vertical coordinate.
	 * @return a 0-simplex with coordinates (x, y).
	 */
	static Point of(float x, float y) {
		return new PointImpl(x, y);
	}

}
