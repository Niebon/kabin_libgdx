package dev.kabin.entities;

import dev.kabin.util.pools.imagemetadata.ImageMetadata;

public interface ImageAnalysisGetter {
    ImageMetadata get(String path, int index);
}
