package dev.kabin.entities;

import dev.kabin.utilities.GameData;
import org.json.JSONObject;

public class EntityParameters {

    private final float x;
    private final float y;
    private final String atlasPath;
    private final float scale;
    private final int layer;

    public EntityParameters(float x, float y, String atlasPath, float scale, int layer) {
        this.x = x;
        this.y = y;
        this.atlasPath = atlasPath;
        this.scale = scale;
        this.layer = layer;
    }

    public float x() {
        return x;
    }

    public float y() {
        return y;
    }

    public float scale() {
        return scale;
    }

    public String atlasPath() {
        return atlasPath;
    }

    int layer() {
        return layer;
    }

    public static class Builder {
        private float x;
        private float y;
        private String atlasPath;
        private float scale;
        private int layer;

        public Builder(JSONObject o) {
            scale = o.getFloat("scale");
            x = o.getInt("x") * GameData.scaleFactor;
            y = o.getInt("y") * GameData.scaleFactor;
            atlasPath = o.getString("atlasPath");
            layer = o.getInt("layer");
        }

        public Builder() {

        }

        public Builder setX(float x) {
            this.x = x;
            return this;
        }

        public Builder setY(float y) {
            this.y = y;
            return this;
        }


        public Builder setAtlasPath(String atlasPath) {
            this.atlasPath = atlasPath;
            return this;
        }

        public Builder setScale(float scale) {
            this.scale = scale;
            return this;
        }

        public Builder setLayer(int layer) {
            this.layer = layer;
            return this;
        }

        public EntityParameters build() {
            return new EntityParameters(
                    x, y, atlasPath, scale, layer
            );
        }


    }
}
