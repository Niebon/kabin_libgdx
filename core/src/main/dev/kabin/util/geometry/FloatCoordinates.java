package dev.kabin.util.geometry;

public interface FloatCoordinates {

    /**
     * @return the horizontal component.
     */
    float x();

    /**
     * @return the vertical component.
     */
    float y();

    /**
     * @param other coordinates.
     * @return the result of the dot product with the other coordinates.
     */
    default float dot(FloatCoordinates other) {
        return x() * other.x() + y() * other.y();
    }

    /**
     * @param other coordinates.
     * @return the z-component of the cross product with the other coordinates.
     */
    default float cross(FloatCoordinates other) {
        return x() * other.y() - y() * other.x();
    }

    default float magnitude() {
        return (float) Math.sqrt(x() * x() + y() * y());
    }

}
