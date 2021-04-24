package dev.kabin.shaders;

import dev.kabin.util.functioninterfaces.FloatSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.Objects;

public record AnchoredLightSourceData(@NotNull LightSourceDataImpl lightSourceData,
                                      @NotNull FloatSupplier anchorX,
                                      @NotNull FloatSupplier anchorY) implements LightSourceData {

    public static AnchoredLightSourceData ofNullables(@Nullable LightSourceDataImpl lightSourceData,
                                                      @Nullable FloatSupplier anchorX,
                                                      @Nullable FloatSupplier anchorY) {
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
        System.out.printf("X was %s, anchorX was %s%n", lightSourceData.getX(), anchorX.get());
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
        System.out.printf("Y was %s, anchorY was %s%n", lightSourceData.getY(), anchorY.get());
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
