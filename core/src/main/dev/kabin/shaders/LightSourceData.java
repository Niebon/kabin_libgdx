package dev.kabin.shaders;

import dev.kabin.util.geometry.ModifiableFloatCoordinates;
import dev.kabin.util.helperinterfaces.JSONSerializable;

public interface LightSourceData extends ModifiableFloatCoordinates, JSONSerializable {

    /**
     * @return the type of this light source.
     */
    LightSourceType getType();

    void setType(LightSourceType type);

    /**
     * @return the tint of this light source.
     */
    Tint getTint();

    /**
     * @return the radius of this light source.
     */
    float getR();

    /**
     * Set the radius to the given value.
     *
     * @param r a new value for the radius of this light source.
     */
    void setR(float r);

    float getAngle();

    void setAngle(float angle);

    float getArcSpan();

    void setArcSpan(float arcSpan);

    default int getUnscaledR() {
        return Math.round(getR());
    }

    default int getUnscaledX() {
        return Math.round(x());
    }

    default int getUnscaledY() {
        return Math.round(y());
    }
}
