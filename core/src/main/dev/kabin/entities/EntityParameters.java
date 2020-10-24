package dev.kabin.entities;

import dev.kabin.global.GlobalData;
import org.json.JSONObject;

import java.util.Map;
import java.util.Optional;

public class EntityParameters {

    private final Map<String, Object> backingMap;
    private final float x;
    private final float y;
    private final String atlasPath;
    private final float scale;
    private final int layer;

    private EntityParameters(float x,
                             float y,
                             String atlasPath,
                             float scale,
                             int layer,
                             Map<String, Object> backingMap) {
        this.x = x;
        this.y = y;
        this.atlasPath = atlasPath;
        this.scale = scale;
        this.layer = layer;
        this.backingMap = backingMap;
    }

    public <T> Optional<T> get(String key, @SuppressWarnings("unused") Class<T> type) {
        //noinspection unchecked
        return Optional.ofNullable((T) backingMap.get(key));
    }

    public Optional<Object> get(String key) {
        return Optional.ofNullable(backingMap.get(key));
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
        private Map<String, Object> backingMap;

        public Builder(JSONObject o) {
            scale = GlobalData.scaleFactor;
            x = o.getInt("x") * scale;
            y = o.getInt("y") * scale;
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

        public Builder setBackingMap(Map<String, Object> backingMap) {
            this.backingMap = backingMap;
            return this;
        }

        public Builder put(String key, Object value) {
            backingMap.put(key, value);
            return this;
        }

        public EntityParameters build() {
            return new EntityParameters(
                    x, y, atlasPath, scale, layer, backingMap
            );
        }


    }
}
