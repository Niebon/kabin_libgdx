package dev.kabin.shaders;

import dev.kabin.util.helperinterfaces.Scalable;
import org.json.JSONObject;

import java.util.Map;

public class LightSourceDataImpl implements LightSourceData, Scalable {

    private final Tint tint;
    private LightSourceType type;
    private float x, y, r, scale;

    private LightSourceDataImpl(LightSourceType type, Tint tint, float x, float y, float r, float scale) {
        this.type = type;
        this.tint = tint;
        this.x = x;
        this.y = y;
        this.r = r;
        this.scale = scale;
    }

    public static LightSourceDataImpl of(JSONObject o, float scale) {
        return new Builder()
                .setR(o.getInt("r") * scale)
                .setX(o.getInt("x") * scale)
                .setY(o.getInt("y") * scale)
                .setTint(Tint.of(o.getJSONObject("tint")))
                .setType(o.getEnum(LightSourceType.class, "type"))
                .setScale(scale)
                .build();
    }

    public static LightSourceDataImpl.Builder builder() {
        return new LightSourceDataImpl.Builder();
    }

    @Override
    public LightSourceType getType() {
        return type;
    }

    @Override
    public void setType(LightSourceType type) {
        this.type = type;
    }

    @Override
    public Tint getTint() {
        return tint;
    }

    @Override
    public float getR() {
        return r;
    }

    @Override
    public void setR(float r) {
        this.r = r;
    }

    @Override
    public float getX() {
        return x;
    }

    @Override
    public void setX(float x) {
        this.x = x;
    }

    @Override
    public float getY() {
        return y;
    }

    @Override
    public void setY(float y) {
        this.y = y;
    }

    @Override
    public JSONObject toJSONObject() {
        return new JSONObject(Map.of(
                "tint", tint.toJSONObject(),
                "x", Math.round(getX() / getScale()),
                "y", Math.round(getY() / getScale()),
                "r", Math.round(getR() / getScale()),
                "type", type.name()
        ));
    }

    @Override
    public float getScale() {
        return scale;
    }

    @Override
    public void setScale(float scale) {
        this.scale = scale;
    }

    public static class Builder {

        private LightSourceType type = LightSourceType.NONE;
        private Tint tint = Tint.of(1, 1, 1);
        private float x, y, r;
        private float scale;

        public Builder setType(LightSourceType type) {
            this.type = type;
            return this;
        }

        public Builder setTint(Tint tint) {
            this.tint = tint;
            return this;
        }

        public Builder setScale(float scale) {
            this.scale = scale;
            return this;
        }

        public Builder setX(float x) {
            this.x = x;
            return this;
        }

        public Builder setY(float y) {
            this.y = y;
            return this;
        }

        public Builder setR(float r) {
            this.r = r;
            return this;
        }

        public LightSourceDataImpl build() {
            return new LightSourceDataImpl(type, tint, x, y, r, scale);
        }
    }
}