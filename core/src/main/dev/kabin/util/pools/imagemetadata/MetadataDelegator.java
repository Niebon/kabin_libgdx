package dev.kabin.util.pools.imagemetadata;


/**
 * Implement this interface to gain direct access to {@link ImageMetadata} methods.
 * This interface gives access to delegated methods.
 */
public interface MetadataDelegator {

    ImageMetadata getMetadata();

    default int getPixelsX() {
        return getMetadata().getPixelsX();
    }

    default int getPixelsY() {
        return getMetadata().getPixelsY();
    }

    default int getLowestPixel() {
        return getMetadata().getLowestPixel();
    }

    default int getHighestPixel() {
        return getMetadata().getHighestPixel();
    }

    default int getLeftmostPixel() {
        return getMetadata().getLeftmostPixel();
    }

    default int getRightmostPixel() {
        return getMetadata().getRightmostPixel();
    }

    default float getPixelMassCenterX() {
        return getMetadata().getPixelMassCenterX();
    }

    default float getPixelMassCenterY() {
        return getMetadata().getPixelMassCenterY();
    }

    default int getLowestPixelFromAbove() {
        return getMetadata().getLowestPixelFromAbove();
    }

    default int getHighestPixelFromBelow() {
        return getMetadata().getHighestPixelFromBelow();
    }

    default int getPixelHeight() {
        return getMetadata().getPixelHeight();
    }

    default int getPixelWidth() {
        return getMetadata().getPixelWidth();
    }

}
