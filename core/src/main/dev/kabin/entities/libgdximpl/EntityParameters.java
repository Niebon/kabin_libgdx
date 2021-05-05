package dev.kabin.entities.libgdximpl;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import dev.kabin.entities.libgdximpl.animation.imageanalysis.ImageMetadataPoolLibgdx;
import dev.kabin.shaders.LightSourceDataImpl;
import dev.kabin.util.NamedObj;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.StreamSupport;

public record EntityParameters(float x, float y, String atlasPath, float scale, int layer,
                               List<NamedObj<LightSourceDataImpl>> lightSourceData,
                               Map<String, Object> backingMap,
                               TextureAtlas textureAtlas,
                               EntityType type,
                               ImageMetadataPoolLibgdx imageAnalysisPool) {

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

    public <T extends Enum<T>> Optional<T> getMaybe(String key, Class<T> clazz) {
        return this.<String>getMaybe(key).map(s -> Enum.valueOf(clazz, s));
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
        private ImageMetadataPoolLibgdx imageAnalysisPool;
        private List<NamedObj<LightSourceDataImpl>> lightSourceData;

        private Builder(JSONObject o, float scale) {
            this.scale = scale;
            x = o.getInt("x") * scale;
            y = o.getInt("y") * scale;
            atlasPath = o.getString("atlas_path");
            layer = o.getInt("layer");
            if (o.get("light_sources") instanceof JSONArray l) {
                lightSourceData = StreamSupport.stream(l.spliterator(), false)
                        .map(JSONObject.class::cast)
                        .map(jsonObject -> new NamedObj<>("default", jsonObject))
                        .map(namedJson -> namedJson.map(jso -> LightSourceDataImpl.of(jso, scale)))
                        .toList();
            } else {
                lightSourceData = o.getJSONObject("light_sources").keySet().stream()
                        .map(key -> new NamedObj<>(key, o.getJSONObject("light_sources").get(key)))
                        .map(namedJson -> namedJson.map(JSONObject.class::cast).map(jso -> LightSourceDataImpl.of(jso, scale)))
                        .toList();
            }

            // Miscellaneous:
            backingMap.putAll(o.toMap());
        }

        private Builder() {

        }

        public Builder setLightSourceDataList(List<NamedObj<LightSourceDataImpl>> lightSourceData) {
            this.lightSourceData = lightSourceData;
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

        public Builder setImageAnalysisPool(ImageMetadataPoolLibgdx imageAnalysisPool) {
            this.imageAnalysisPool = imageAnalysisPool;
            return this;
        }

        public EntityParameters build() {
            return new EntityParameters(x,
                    y,
                    atlasPath,
                    scale,
                    layer,
                    lightSourceData,
                    backingMap,
                    textureAtlas,
                    type,
                    imageAnalysisPool);
        }


    }
}
