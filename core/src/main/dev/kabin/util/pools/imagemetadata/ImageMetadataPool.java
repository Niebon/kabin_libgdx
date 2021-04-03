package dev.kabin.util.pools.imagemetadata;

public interface ImageMetadataPool {
    ImageMetadata findAnalysis(String path, int index);
}
