package dev.kabin.shaders;

import org.json.JSONObject;

import java.util.Map;

import static dev.kabin.util.Functions.tryGet;

public class LightSourceDataImpl implements LightSourceData {

    private final Tint tint;
    private LightSourceType type;
    private float x, y, r, angle, arcSpan;

    private LightSourceDataImpl(LightSourceType type, Tint tint, float x, float y, float r, float angle, float arcSpan) {
        this.type = type;
        this.tint = tint;
        this.x = x;
        this.y = y;
        this.r = r;
        this.angle = angle;
        this.arcSpan = arcSpan;
    }

    public static LightSourceDataImpl of(JSONObject o) {
        return new Builder()

                // No big deal if some of these become zero...
                .setR(tryGet(() -> o.getFloat("r")).orElse(0f))
                .setX(tryGet(() -> o.getFloat("x")).orElse(0f))
                .setY(tryGet(() -> o.getFloat("y")).orElse(0f))
                .setAngle(tryGet(() -> o.getFloat("angle")).orElse(0f))
                .setArcSpan(tryGet(() -> o.getFloat("arc_span")).orElse(tryGet(() -> o.getFloat("width")).orElse(0f)))

                // The rest should throw.
                .setTint(Tint.of(o.getJSONObject("tint")))
                .setType(o.getEnum(LightSourceType.class, "type"))
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
                "x", Math.round(x),
                "y", Math.round(y),
                "r", Math.round(r),
                "angle", angle,
                "arc_span", arcSpan,
                "type", type.name()
        ));
    }

    @Override
    public float getAngle() {
        return angle;
    }

    @Override
    public void setAngle(float angle) {
        this.angle = angle;
    }

    @Override
    public float getArcSpan() {
        return arcSpan;
    }

    @Override
    public void setArcSpan(float arcSpan) {
        this.arcSpan = arcSpan;
    }

    public static class Builder {

        private LightSourceType type = LightSourceType.NONE;
        private Tint tint = Tint.of(1, 1, 1);
        private float x, y, r, angle, arcSpan;

        public Builder setType(LightSourceType type) {
            this.type = type;
            return this;
        }

        public Builder setTint(Tint tint) {
            this.tint = tint;
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

        public Builder setAngle(float angle) {
            this.angle = angle;
            return this;
        }

        public Builder setArcSpan(float arcSpan) {
            this.arcSpan = arcSpan;
            return this;
        }

        public LightSourceDataImpl build() {
            return new LightSourceDataImpl(type, tint, x, y, r, angle, arcSpan);
        }
    }
}
