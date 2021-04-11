package dev.kabin.util.pools.imagemetadata;


/**
 * Implement this interface to gain direct access to {@link ImageMetadata} methods.
 * This interface gives access to delegated methods.
 */
public interface MetadataDelegator {

    ImageMetadata getImageMetadata();

    default int getPixelsX() {
        return getImageMetadata().getPixelsX();
    }

    default int getPixelsY() {
        return getImageMetadata().getPixelsY();
    }

    default int getLowestPixel() {
        return getImageMetadata().getLowestPixel();
    }

    default int getHighestPixel() {
        return getImageMetadata().getHighestPixel();
    }

    default int getLeftmostPixel() {
        return getImageMetadata().getLeftmostPixel();
    }

    default int getRightmostPixel() {
        return getImageMetadata().getRightmostPixel();
    }

    default float getPixelMassCenterX() {
        return getImageMetadata().getPixelMassCenterX();
    }

    default float getPixelMassCenterY() {
        return getImageMetadata().getPixelMassCenterY();
    }

    default int getLowestPixelFromAbove() {
        return getImageMetadata().getLowestPixelFromAbove();
    }

    default int getHighestPixelFromBelow() {
        return getImageMetadata().getHighestPixelFromBelow();
    }

    default int getPixelHeight() {
        return getImageMetadata().getPixelHeight();
    }

    default int getPixelWidth() {
        return getImageMetadata().getPixelWidth();
    }

}
