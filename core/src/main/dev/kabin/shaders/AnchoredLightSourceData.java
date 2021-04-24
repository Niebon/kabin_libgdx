package dev.kabin.shaders;

import dev.kabin.util.functioninterfaces.FloatSupplier;
import org.json.JSONObject;

import java.util.Objects;

public record AnchoredLightSourceData(LightSourceDataImpl lightSourceData,
                                      FloatSupplier anchorX,
                                      FloatSupplier anchorY) implements LightSourceData {

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

    public void setXRelToAnchor(float x) {
        lightSourceData.setX(x);
    }

    public void setYRelToAnchor(float y) {
        lightSourceData.setY(y);
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
