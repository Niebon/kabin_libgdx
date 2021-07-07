package dev.kabin.entities.libgdximpl.animation.imageanalysis;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import dev.kabin.util.Functions;
import dev.kabin.util.geometry.points.PointInt;
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
    private final int lowestArtPixel;
    private final int highestArtPixel;
    private final int leftmostArtPixel;
    private final int rightmostArtPixel;
    private final int artHeight;
    private final int artWidth;
    private final int artMassCenterXAsInt;
    private final int artMassCenterYAsInt;
    private final int imgWith;
    private final int imgHeight;
    private final float
            artMassCenterX,
            artMassCenterY;
    private int lowestArtPixelFromAbove;
    private int highestArtPixelFromBelow; //TODO find this

    private ImageMetadataLibgdx() {
        lowestArtPixel = 0;
        highestArtPixel = 0;
        leftmostArtPixel = 0;
        rightmostArtPixel = 0;
        artHeight = 0;
        artWidth = 0;
        artMassCenterXAsInt = 0;
        artMassCenterYAsInt = 0;
        imgWith = 0;
        imgHeight = 0;
        artMassCenterX = 0;
        artMassCenterY = 0;
    }

    public ImageMetadataLibgdx(
            @NotNull TextureAtlas atlas,
            @NotNull String path,
            int index) {
        final int width, height;
        final Optional<TextureAtlas.AtlasRegion> atlasRegionMaybe = Optional.ofNullable(atlas.getRegions()
                .select(a -> path.equals(a.toString()) && index == a.index).iterator().next());
        if (atlasRegionMaybe.isEmpty()) {
            lowestArtPixel = 0;
            highestArtPixel = 0;
            leftmostArtPixel = 0;
            rightmostArtPixel = 0;
            artHeight = 0;
            artWidth = 0;
            artMassCenterXAsInt = 0;
            artMassCenterYAsInt = 0;
            imgWith = 0;
            imgHeight = 0;
            artMassCenterX = 0;
            artMassCenterY = 0;
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
                        if (j > lowestArtPixelFromAbove) lowestArtPixelFromAbove = j;
                        continue outer;
                    }

                }
            }
        }

        this.lowestArtPixel = Functions.transformY(lowestPixel, height);
        this.highestArtPixel = Functions.transformY(highestPixel, height);
        this.rightmostArtPixel = rightmostPixel;
        this.leftmostArtPixel = leftmostPixel;

        artMassCenterX = (float) sumx / nx;
        artMassCenterY = Functions.transformY((float) sumy / ny, height);
        artMassCenterXAsInt = Math.round(artMassCenterX);
        artMassCenterYAsInt = Functions.transformY(Math.round(artMassCenterY), height);

        artHeight = lowestPixel - highestPixel;
        artWidth = rightmostPixel - leftmostPixel;
        imgWith = width;
        imgHeight = height;
    }

    /**
     * An analysis object which holds no data.
     */
    public static ImageMetadata emptyAnalysis() {
        return EMPTY_ANALYSIS;
    }

    @Override
    public List<PointInt> getArtPixelProfile() {
        return new ArrayList<>(pixelProfile);
    }

    @Override
    public int lowestArtPixel() {
        return lowestArtPixel;
    }

    @Override
    public int highestArtPixel() {
        return highestArtPixel;
    }

    @Override
    public int leftmostArtPixel() {
        return leftmostArtPixel;
    }

    @Override
    public int rightmostArtPixel() {
        return rightmostArtPixel;
    }

    @Override
    public int lowestArtPixelFromAbove() {
        return lowestArtPixelFromAbove;
    }

    @Override
    public int highestArtPixelFromBelow() {
        return highestArtPixelFromBelow;
    }

    @Override
    public int artHeight() {
        return artHeight;
    }

    @Override
    public int artWidth() {
        return artWidth;
    }

    @Override
    public float artMassCenterX() {
        return artMassCenterX;
    }

    @Override
    public float artMassCenterY() {
        return artMassCenterY;
    }

    @Override
    public int artMassCenterXAsInt() {
        return artMassCenterXAsInt;
    }

    @Override
    public int artMassCenterYAsInt() {
        return artMassCenterYAsInt;
    }

    @Override
    public int imgWidth() {
        return imgWith;
    }

    @Override
    public int imgHeight() {
        return imgHeight;
    }

}
