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

}
