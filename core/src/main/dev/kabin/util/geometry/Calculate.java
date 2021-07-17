package dev.kabin.util.geometry;

import dev.kabin.util.geometry.points.PointFloat;

final class Calculate {

    /**
     * Calculates the cross product p1p2 x p2p3.
     *
     * @param p1 the first point.
     * @param p2 the second point.
     * @param p3 the third point.
     * @return the sign of the direction of the turn p1 -> p2 -> p3. If the direction goes to the left, then the sign is positive.
     * Otherwise it is negative.
     */
    static float turnSign(PointFloat p1, PointFloat p2, PointFloat p3) {

        class Helper {
            private static PointFloat direction(PointFloat p1, PointFloat p2) {
                return PointFloat.immutable(p2.x() - p1.x(), p2.y() - p1.y());
            }
        }

        var p1p2 = Helper.direction(p1, p2);
        var p2p3 = Helper.direction(p2, p3);
        return p1p2.cross(p2p3);
    }

}
