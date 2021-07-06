package dev.kabin.util.deltacomplexes;

import dev.kabin.util.helperinterfaces.ModifiableFloatCoordinates;
/**
 * A 0-simplex is a point (x,y) with modifiable {@code float} coordinates.
 */
public interface Simplex0 extends ModifiableFloatCoordinates {

	/**
	 * A factory constructor.
	 *
	 * @param x horizontal coordinate.
	 * @param y vertical coordinate.
	 * @return a 0-simplex with coordinates (x, y).
	 */
	static Simplex0 of(float x, float y) {
		return new Simplex0Impl(x, y);
	}

}
