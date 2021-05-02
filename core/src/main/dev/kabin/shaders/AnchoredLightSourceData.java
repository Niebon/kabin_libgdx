package dev.kabin.shaders;

import dev.kabin.util.fp.FloatSupplier;
import org.json.JSONObject;

import java.util.Objects;

/**
 * An anchored light source data is a wrapper for a light source data that is attached relative
 * to an anchor. The anchor may be modifiable, as such the anchor is supplied by lambdas.
 */
public record AnchoredLightSourceData(LightSourceDataImpl lightSourceData,
                                      FloatSupplier anchorX,
                                      FloatSupplier anchorY) implements LightSourceData {

    /**
     * A helper constructors that accepts null values and which, if null values are provided,
     * supplies nonnull values for these instead.
     *
     * @param lightSourceData the light source value.
     * @param anchorX         the anchor for the horizontal coordinate.
     * @param anchorY         the anchor for the vertical coordinate.
     * @return a new instance of an anchored light source data.
     */
    public static AnchoredLightSourceData ofNullables(LightSourceDataImpl lightSourceData,
                                                      FloatSupplier anchorX,
                                                      FloatSupplier anchorY) {
        return new AnchoredLightSourceData(
                Objects.requireNonNullElse(lightSourceData, LightSourceDataImpl.builder().build()),
                Objects.requireNonNullElse(anchorX, () -> 0f),
                Objects.requireNonNullElse(anchorY, () -> 0f)
        );
    }

    @Override
    public float getAngle() {
        return lightSourceData.getAngle();
    }

    @Override
    public void setAngle(float angle) {
        lightSourceData.setAngle(angle);
    }

    @Override
    public float getWidth() {
        return lightSourceData.getWidth();
    }

    @Override
    public void setWidth(float width) {
        lightSourceData.setWidth(width);
    }

    @Override
    public LightSourceType getType() {
        return lightSourceData.getType();
    }

    @Override
    public void setType(LightSourceType type) {
        lightSourceData.setType(type);
    }

    @Override
    public Tint getTint() {
        return lightSourceData.getTint();
    }

    @Override
    public float getR() {
        return lightSourceData.getR();
    }

    @Override
    public void setR(float r) {
        lightSourceData.setR(r);
    }

    @Override
    public float getX() {
        return lightSourceData.getX() + anchorX.get();
    }

    @Override
    public void setX(float x) {
        lightSourceData.setX(x + anchorX.get());
    }

    public float getUnscaledXRelToAnchor() {
        return lightSourceData.getUnscaledX();
    }

    public void setUnscaledXRelToAnchor(float x) {
        lightSourceData.setX(x * lightSourceData.getScale());
    }

    @Override
    public float getY() {
        return lightSourceData.getY() + anchorY.get();
    }

    @Override
    public void setY(float y) {
        lightSourceData.setY(y + anchorY.get());
    }

    public float getUnscaledYRelToAnchor() {
        return lightSourceData.getUnscaledY();
    }

    public void setUnscaledYRelToAnchor(float y) {
        lightSourceData.setY(y * lightSourceData.getScale());
    }

    @Override
    public JSONObject toJSONObject() {
        return lightSourceData.toJSONObject();
    }

    @Override
    public float getScale() {
        return lightSourceData.getScale();
    }

    public void setScale(float scale) {
        lightSourceData.setScale(scale);
    }
}
