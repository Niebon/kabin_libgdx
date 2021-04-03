package dev.kabin.entities.libgdximpl.animation;

import dev.kabin.util.pools.imagemetadata.ImageMetadata;

public interface ImageAnalysisSupplier {
    ImageMetadata get(String path, int index);
}
