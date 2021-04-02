package dev.kabin.entities.impl;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class EntityParameters {

    private final Map<String, Object> backingMap;
    private final float x;
    private final float y;
    private final String atlasPath;
    private final float scale;
    private final int layer;
    private final TextureAtlas textureAtlas;
    private final EntityType type;


    private EntityParameters(float x,
                             float y,
                             String atlasPath,
                             float scale,
                             int layer,
                             Map<String, Object> backingMap,
                             TextureAtlas textureAtlas, EntityType type) {
        this.x = x;
        this.y = y;
        this.atlasPath = atlasPath;
        this.scale = scale;
        this.layer = layer;
        this.backingMap = backingMap;
        this.textureAtlas = textureAtlas;
        this.type = type;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(JSONObject o, float scale) {
        return new Builder(o, scale);
    }

    public <T> Optional<T> getMaybe(String key) {
        //noinspection unchecked
        return Optional.ofNullable((T) backingMap.get(key));
    }

    public TextureAtlas getTextureAtlas() {
        return textureAtlas;
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

    public int layer() {
        return layer;
    }

    public EntityType getType() {
        return type;
    }

    public static class Builder {
        private float x;
        private float y;
        private String atlasPath;
        private float scale;
        private int layer;
        private Map<String, Object> backingMap = new HashMap<>();
        private TextureAtlas textureAtlas;
        private EntityType type;

        private Builder(JSONObject o, float scale) {
            this.scale = scale;
            x = o.getInt("x") * scale;
            y = o.getInt("y") * scale;
            atlasPath = o.getString("atlasPath");
            layer = o.getInt("layer");

            // Miscellaneous:
            backingMap.putAll(o.toMap());
        }

        private Builder() {

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

        public Builder setTextureAtlas(TextureAtlas textureAtlas) {
            this.textureAtlas = textureAtlas;
            return this;
        }

        public Builder setEntityType(EntityType type) {
            this.type = type;
            return this;
        }


        public Builder put(String key, Object value) {
            Optional.ofNullable(backingMap).or(() -> {
                backingMap = new HashMap<>();
                return Optional.of(backingMap);
            }).orElseThrow().put(key, value);
            return this;
        }

        public EntityParameters build() {
            return new EntityParameters(
                    x, y, atlasPath, scale, layer, backingMap,
                    textureAtlas, type);
        }


    }
}
