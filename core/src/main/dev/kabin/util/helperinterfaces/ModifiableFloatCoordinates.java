package dev.kabin.util.helperinterfaces;

import dev.kabin.util.points.PointOld;
import dev.kabin.util.points.PointFloatOld;
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
     * The default implementation creates a new {@link PointOld} instance for each call.
     * If you want to return a cached point, it is advised to use {@link #recordPosition(PointFloatOld)}.
     *
     * @return the current position data as a {@link PointOld} isntance.
     */
    @Contract("->new")
    default PointFloatOld getPosition() {
        return PointOld.of(getX(), getY());
    }

    /**
     * Writes the position data associated with this into the given point.
     *
     * @param positionRecord where to record the position data.
     * @return the input to the method.
     */
    @Contract("_->param1")
    default PointFloatOld recordPosition(PointFloatOld positionRecord) {
        return positionRecord.setX(getX()).setY(getY());
    }

}
