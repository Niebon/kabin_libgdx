package dev.kabin.util.pools.imagemetadata;


/**
 * Implement this interface to gain direct access to delegated {@link ImageMetadata} methods.
 */
public interface ImgMetadataDelegator {

    /**
     * @return the image metadata associated with this instance.
     */
    ImageMetadata imgMetadata();

    //=============================================== Delegated methods ===============================================

    default int imgWidth() {
        return imgMetadata().imgWidth();
    }

    default int imgHeight() {
        return imgMetadata().imgHeight();
    }

    default int lowestArtPixel() {
        return imgMetadata().lowestArtPixel();
    }

    default int highestArtPixel() {
        return imgMetadata().highestArtPixel();
    }

    default int leftmostArtPixel() {
        return imgMetadata().leftmostArtPixel();
    }

    default int rightmostArtPixel() {
        return imgMetadata().rightmostArtPixel();
    }

    default float artMassCenterX() {
        return imgMetadata().artMassCenterX();
    }

    default float artMassCenterY() {
        return imgMetadata().artMassCenterY();
    }

    default int lowestArtPixelFromAbove() {
        return imgMetadata().lowestArtPixelFromAbove();
    }

    default int highestArtPixelFromBelow() {
        return imgMetadata().highestArtPixelFromBelow();
    }

    default int artHeight() {
        return imgMetadata().artHeight();
    }

    default int artWidth() {
        return imgMetadata().artWidth();
    }

}
