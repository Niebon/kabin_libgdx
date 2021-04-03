package dev.kabin.util.pools.imagemetadata;

import dev.kabin.util.points.PointInt;

import java.util.List;

public interface ImageMetadata {

    List<PointInt> getPixelProfile();

    int getLowestPixel();

    int getHighestPixel();

    int getLeftmostPixel();

    int getRightmostPixel();

    int getLowestPixelFromAbove();

    int getHighestPixelFromBelow();

    int getPixelHeight();

    int getPixelWidth();

    float getPixelMassCenterX();

    float getPixelMassCenterY();

    int getPixelMassCenterXInt();

    int getPixelMassCenterYInt();

    int getPixelsX();

    int getPixelsY();

}
