package dev.kabin.utilities.helperinterfaces;

import dev.kabin.geometry.points.Point;
import dev.kabin.geometry.points.PointFloat;
import org.jetbrains.annotations.Contract;

public interface ModifiableFloatCoordinates {

    float getX();

    void setX(float x);

    float getY();

    void setY(float y);

    default void setPos(float x, float y) {
        setX(x);
        setY(y);
    }

    /**
     * A helper procedure to consider the current position as a point.
     * The default implementation creates a new {@link Point} instance for each call.
     * If you want to return a cached point, it is advised to use {@link #recordPosition(PointFloat)}.
     *
     * @return the current position data as a {@link Point} isntance.
     */
    @Contract("->new")
    default PointFloat getPosition() {
        return Point.of(getX(), getY());
    }

    /**
     * Writes the position data associated with this into the given point.
     *
     * @param positionRecord where to record the position data.
     * @return the input to the method.
     */
    @Contract("_->param1")
    default PointFloat recordPosition(PointFloat positionRecord) {
        return positionRecord.setX(getX()).setY(getY());
    }

}
