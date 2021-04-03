package dev.kabin.entities.libgdximpl.animation.imageanalysis;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import dev.kabin.util.Functions;
import dev.kabin.util.points.PointInt;
import dev.kabin.util.pools.ImagePool;
import dev.kabin.util.pools.imagemetadata.ImageMetadata;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static dev.kabin.GlobalData.TEXTURES_PATH;

public class ImageMetadataLibgdx implements ImageMetadata {

    private static final ImageMetadataLibgdx EMPTY_ANALYSIS = new ImageMetadataLibgdx();
    private final List<PointInt> pixelProfile = new ArrayList<>();
    private final int lowestPixel;
    private final int highestPixel;
    private final int leftmostPixel;
    private final int rightmostPixel;
    private final int pixelHeight;
    private final int pixelWidth;
    private final int pixelMassCenterXInt;
    private final int pixelMassCenterYInt;
    private final int pixelsX;
    private final int pixelsY;
    private final float
            pixelMassCenterX,
            pixelMassCenterY;
    private int lowestPixelFromAbove;
    private int highestPixelFromBelow; //TODO find this

    private ImageMetadataLibgdx() {
        lowestPixel = 0;
        highestPixel = 0;
        leftmostPixel = 0;
        rightmostPixel = 0;
        pixelHeight = 0;
        pixelWidth = 0;
        pixelMassCenterXInt = 0;
        pixelMassCenterYInt = 0;
        pixelsX = 0;
        pixelsY = 0;
        pixelMassCenterX = 0;
        pixelMassCenterY = 0;
    }

    public ImageMetadataLibgdx(
            @NotNull TextureAtlas atlas,
            @NotNull String path,
            int index) {
        final int width, height;
        final Optional<TextureAtlas.AtlasRegion> atlasRegionMaybe = Optional.ofNullable(atlas.getRegions()
                .select(a -> path.equals(a.toString()) && index == a.index).iterator().next());
        if (atlasRegionMaybe.isEmpty()) {
            lowestPixel = 0;
            highestPixel = 0;
            leftmostPixel = 0;
            rightmostPixel = 0;
            pixelHeight = 0;
            pixelWidth = 0;
            pixelMassCenterXInt = 0;
            pixelMassCenterYInt = 0;
            pixelsX = 0;
            pixelsY = 0;
            pixelMassCenterX = 0;
            pixelMassCenterY = 0;
            return;
        }

        final TextureAtlas.AtlasRegion atlasRegion = atlasRegionMaybe.get();
        int minX = atlasRegion.getRegionX();
        int minY = atlasRegion.getRegionY();
        width = atlasRegion.getRegionWidth();
        height = atlasRegion.getRegionHeight();


        final BufferedImage bufferedImage = ImagePool.findBufferedImage(TEXTURES_PATH);


        // Find pixel mass center.
        int sumx = 0;
        int nx = 0;
        int sumy = 0;
        int ny = 0;

        // Find lowest pixel.
        int lowestPixel = 0;
        int highestPixel = height;
        int rightmostPixel = 0;
        int leftmostPixel = width;

        // Find lowest, highest, rightmost, leftmost pixels.
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {

                final double alphaValue = (0xFF & (bufferedImage.getRGB(i + minX, j + minY) >> 24));
                final boolean collision = alphaValue > 0;

                if (collision && j > lowestPixel) lowestPixel = j;
                if (collision && j < highestPixel) highestPixel = j;
                if (collision && i > rightmostPixel) rightmostPixel = i;
                if (collision && i < leftmostPixel) leftmostPixel = i;

                if (collision) {
                    sumx = sumx + i;
                    nx = nx + 1;
                    sumy = sumy + j;
                    ny = ny + 1;
                    pixelProfile.add(PointInt.immutable(i, j));
                }
            }
        }

        // Find lowest pixel from above.
        {
            outer:
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {

                    final double alphaValue = (0xFF & (bufferedImage.getRGB(i + minX, j + minY) >> 24));
                    final boolean collision = alphaValue > 0;// true means there is collision/visible pixels

                    if (collision) {
                        if (j > lowestPixelFromAbove) lowestPixelFromAbove = j;
                        continue outer;
                    }

                }
            }
        }

        this.lowestPixel = Functions.transformY(lowestPixel, height);
        this.highestPixel = Functions.transformY(highestPixel, height);
        this.rightmostPixel = rightmostPixel;
        this.leftmostPixel = leftmostPixel;

        pixelMassCenterX = (float) sumx / nx;
        pixelMassCenterY = Functions.transformY((float) sumy / ny, height);
        pixelMassCenterXInt = Math.round(pixelMassCenterX);
        pixelMassCenterYInt = Functions.transformY(Math.round(pixelMassCenterY), height);

        pixelHeight = lowestPixel - highestPixel;
        pixelWidth = rightmostPixel - leftmostPixel;
        pixelsX = width;
        pixelsY = height;
    }

    /**
     * An analysis object which holds no data.
     */
    public static ImageMetadata emptyAnalysis() {
        return EMPTY_ANALYSIS;
    }

    @Override
    public List<PointInt> getPixelProfile() {
        return new ArrayList<>(pixelProfile);
    }

    @Override
    public int getLowestPixel() {
        return lowestPixel;
    }

    @Override
    public int getHighestPixel() {
        return highestPixel;
    }

    @Override
    public int getLeftmostPixel() {
        return leftmostPixel;
    }

    @Override
    public int getRightmostPixel() {
        return rightmostPixel;
    }

    @Override
    public int getLowestPixelFromAbove() {
        return lowestPixelFromAbove;
    }

    @Override
    public int getHighestPixelFromBelow() {
        return highestPixelFromBelow;
    }

    @Override
    public int getPixelHeight() {
        return pixelHeight;
    }

    @Override
    public int getPixelWidth() {
        return pixelWidth;
    }

    @Override
    public float getPixelMassCenterX() {
        return pixelMassCenterX;
    }

    @Override
    public float getPixelMassCenterY() {
        return pixelMassCenterY;
    }

    @Override
    public int getPixelMassCenterXInt() {
        return pixelMassCenterXInt;
    }

    @Override
    public int getPixelMassCenterYInt() {
        return pixelMassCenterYInt;
    }

    @Override
    public int getPixelsX() {
        return pixelsX;
    }

    @Override
    public int getPixelsY() {
        return pixelsY;
    }

}
