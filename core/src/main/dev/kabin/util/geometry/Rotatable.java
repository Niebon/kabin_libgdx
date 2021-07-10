package dev.kabin.util.geometry;

public interface Rotatable {

    /**
     * Rotates this about the given pivot point.
     *
     * @param pivotX   x coordinate of a pivot point.
     * @param pivotY   y coordinate of a pivot point.
     * @param angleRad the angle (in radians) that these coordinates are to be rotated by.
     */
    void rotate(float pivotX, float pivotY, double angleRad);

    /**
     * Rotates this about the origin.
     *
     * @param angleRad the angle (in radians) that these coordinates are to be rotated by.
     */
    default void rotate(double angleRad) {
        rotate(0, 0, angleRad);
    }

}
