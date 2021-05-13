package dev.kabin.util.pools.imagemetadata;

import dev.kabin.util.points.PointInt;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.List;

/**
 * Image metadata for an image consisting of pixels, i.e. non-negative <i>red,green,blue,alpha</i> levels,
 * placed in a grid with fixed height and width).
 * The pixels are placed in a regular <i>xy</i> coordinate system (e.g. <i>y</i> goes upwards).
 * We say that a pixel in the given image is an <b>art pixel</b> if it has alpha channel different from zero.
 */
public interface ImageMetadata {

    /**
     * @return a list that contains all art pixels in the image that this image-metadata represents.
     */
    @UnmodifiableView
    List<PointInt> getArtPixelProfile();

    /**
     * @return the <i>y</i>-coordinate of the highest art pixel (minimal among <i>y</i>).
     */
    int lowestArtPixel();

    /**
     * @return the <i>y</i>-coordinate of the highest art pixel (maximal among <i>y</i>).
     */
    int highestArtPixel();

    /**
     * @return the <i>x</i>-coordinate of the leftmost art pixel (minimal among <i>x</i>).
     */
    int leftmostArtPixel();

    /**
     * @return the <i>x</i>-coordinate of the rightmost art pixel (maximal among <i>x</i>).
     */
    int rightmostArtPixel();

    /**
     * @return the <i>y</i>-coordinate of the lowest art pixel (minimal among <i>y</i>) with the property
     * that all pixels above are not art pixels.
     * @see #highestArtPixelFromBelow()
     */
    int lowestArtPixelFromAbove();

    /**
     * @return the <i>y</i>-coordinate of the highest art pixel (maximal among <i>y</i>) with the property
     * that all pixels below are not art pixels.
     * @see #lowestArtPixelFromAbove()
     */
    int highestArtPixelFromBelow();

    /**
     * @return the art height of this image. Art height means the maximal <i>y</i>-distance among pairs of pixels that have non-zero alpha channel.
     * @see #imgHeight()
     */
    int artHeight();

    /**
     * @return the art height of this image. Art height means the maximal <i>x</i>-distance among pairs of pixels that have non-zero alpha channel.
     * @see #imgWidth()
     */
    int artWidth();

    /**
     * @return the horizontal mass center of art pixels.
     */
    float artMassCenterX();

    /**
     * @return the vertical mass center of art pixels.
     */
    float artMassCenterY();

    /**
     * @return the horizontal mass center of art pixels.
     */
    int artMassCenterXAsInt();

    /**
     * @return the vertical mass center of art pixels.
     */
    int artMassCenterYAsInt();

    /**
     * @return the width of the image.
     * @see #artWidth()
     */
    int imgWidth();

    /**
     * @return the height of the image.
     * @see #artHeight()
     */
    int imgHeight();

}
