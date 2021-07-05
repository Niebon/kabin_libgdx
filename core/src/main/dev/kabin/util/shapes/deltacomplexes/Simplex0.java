package dev.kabin.util.shapes.deltacomplexes;

import dev.kabin.util.helperinterfaces.ModifiableFloatCoordinates;
import dev.kabin.util.points.ModifiablePointFloat;

/**
 * A 0-simplex is a point (x,y) with modifiable {@code float} coordinates.
 */
public interface Simplex0 extends ModifiableFloatCoordinates {

	/**
	 * A factory constructor.
	 *
	 * @param x horizontal coordinate.
	 * @param y vertical coordinate.
	 * @return a 0 simplex with coordinates (0,0).
	 */
	static Simplex0 of(float x, float y) {
		return new Simplex0Impl(new ModifiablePointFloat(x, y));
	}

}
