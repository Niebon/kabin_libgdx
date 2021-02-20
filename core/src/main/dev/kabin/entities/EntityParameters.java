package dev.kabin.entities;

import dev.kabin.MainGame;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class EntityParameters {

    private final Context context;
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
                             Map<String, Object> backingMap,
                             Context context) {
        this.context = context;
        this.x = x;
        this.y = y;
        this.atlasPath = atlasPath;
        this.scale = scale;
        this.layer = layer;
        this.backingMap = backingMap;
    }

    public <T> Optional<T> getMaybe(String key) {
        //noinspection unchecked
        return Optional.ofNullable((T) backingMap.get(key));
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

    public Context getContext() {
        return context;
    }

    int layer() {
        return layer;
    }

    public enum Context {TEST, PRODUCTION}

    public static class Builder {
        private float x;
        private float y;
        private String atlasPath;
        private float scale;
        private int layer;
        private Map<String, Object> backingMap = new HashMap<>();
        private Context context = Context.PRODUCTION;

        public Builder(JSONObject o) {
            scale = MainGame.scaleFactor;
            x = o.getInt("x") * scale;
            y = o.getInt("y") * scale;
            atlasPath = o.getString("atlasPath");
            layer = o.getInt("layer");

            // Miscellaneous:
            backingMap.putAll(o.toMap());
        }

        public Builder() {

        }

        public static Builder testParameters(){
            return new Builder().setContext(Context.TEST);
        }

        public Builder setX(float x) {
            this.x = x;
            return this;
        }

        public Builder setY(float y) {
            this.y = y;
            return this;
        }

        public Builder setContext(Context context) {
            this.context = context;
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
            Optional.ofNullable(backingMap).or(() -> {
                backingMap = new HashMap<>();
                return Optional.of(backingMap);
            }).orElseThrow().put(key, value);
            return this;
        }

        public EntityParameters build() {
            return new EntityParameters(
                    x, y, atlasPath, scale, layer, backingMap, context
            );
        }


    }
}
