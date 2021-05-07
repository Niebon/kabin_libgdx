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

public record EntityParameters(float x, float y, String atlasPath, int layer,
                               List<NamedObj<LightSourceDataImpl>> lightSourceData,
                               Map<String, Object> backingMap,
                               TextureAtlas textureAtlas,
                               EntityType type,
                               ImageMetadataPoolLibgdx imageAnalysisPool) {

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(JSONObject o) {
        return new Builder(o);
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
        private int layer;
        private Map<String, Object> backingMap = new HashMap<>();
        private TextureAtlas textureAtlas;
        private EntityType type;
        private ImageMetadataPoolLibgdx imageAnalysisPool;
        private List<NamedObj<LightSourceDataImpl>> lightSourceData = List.of();

        private Builder(JSONObject o) {
            x = o.getInt("x");
            y = o.getInt("y");
            atlasPath = o.getString("atlas_path");
            layer = o.getInt("layer");
            if (o.get("light_sources") instanceof JSONArray l) {
                lightSourceData = StreamSupport.stream(l.spliterator(), false)
                        .map(JSONObject.class::cast)
                        .map(jsonObject -> new NamedObj<>("default", jsonObject))
                        .map(namedJson -> namedJson.map(LightSourceDataImpl::of))
                        .toList();
            } else {
                lightSourceData = o.getJSONObject("light_sources").keySet().stream()
                        .map(key -> new NamedObj<>(key, o.getJSONObject("light_sources").get(key)))
                        .map(namedJson -> namedJson.map(JSONObject.class::cast).map(LightSourceDataImpl::of))
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
                    layer,
                    lightSourceData,
                    backingMap,
                    textureAtlas,
                    type,
                    imageAnalysisPool);
        }


    }
}
