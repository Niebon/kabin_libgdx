package dev.kabin.entities.libgdximpl.animation.imageanalysis;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import dev.kabin.util.pools.imagemetadata.ImageMetadata;
import dev.kabin.util.pools.imagemetadata.ImageMetadataPool;

import java.util.HashMap;
import java.util.Map;

public class ImageMetadataPoolLibgdx implements ImageMetadataPool {

    private final TextureAtlas atlas;

    private final Map<String, Map<Integer, ImageMetadata>> data = new HashMap<>();

    public ImageMetadataPoolLibgdx(TextureAtlas atlas) {
        this.atlas = atlas;
    }

    @Override
    public ImageMetadata findAnalysis(String path, int index) {
        if (!data.containsKey(path) || !data.get(path).containsKey(index)) {
            data.putIfAbsent(path, new HashMap<>());
            data.get(path).put(index, new ImageMetadataLibgdx(atlas, path, index));
        }
        return data.get(path).get(index);
    }

}
