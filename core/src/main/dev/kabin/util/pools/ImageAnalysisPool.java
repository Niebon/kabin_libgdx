package dev.kabin.util.pools;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import dev.kabin.util.Functions;
import dev.kabin.util.points.PointInt;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.util.*;

import static dev.kabin.GlobalData.TEXTURES_PATH;

public class ImageAnalysisPool {

    private static final Map<String, Map<Integer, Analysis>> data = new HashMap<>();

    public static Analysis findAnalysis(TextureAtlas atlas, String path, int index) {
        if (!data.containsKey(path) || !data.get(path).containsKey(index)) {
            data.putIfAbsent(path, new HashMap<>());
            data.get(path).put(index, new Analysis(atlas, path, index));
        }
        return data.get(path).get(index);
    }

    public static class Analysis {

        private static final Analysis EMPTY_ANALYSIS = new Analysis();
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

        private Analysis() {
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

        public Analysis(
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
        public static Analysis emptyAnalysis() {
            return EMPTY_ANALYSIS;
        }

        public List<PointInt> getPixelProfile() {
            return new ArrayList<>(pixelProfile);
        }

        public int getLowestPixel() {
            return lowestPixel;
        }

        public int getHighestPixel() {
            return highestPixel;
        }

        public int getLeftmostPixel() {
            return leftmostPixel;
        }

        public int getRightmostPixel() {
            return rightmostPixel;
        }

        public int getLowestPixelFromAbove() {
            return lowestPixelFromAbove;
        }

        public int getHighestPixelFromBelow() {
            return highestPixelFromBelow;
        }

        public int getPixelHeight() {
            return pixelHeight;
        }

        public int getPixelWidth() {
            return pixelWidth;
        }

        public float getPixelMassCenterX() {
            return pixelMassCenterX;
        }

        public float getPixelMassCenterY() {
            return pixelMassCenterY;
        }

        public int getPixelMassCenterXInt() {
            return pixelMassCenterXInt;
        }

        public int getPixelMassCenterYInt() {
            return pixelMassCenterYInt;
        }

        public int getPixelsX() {
            return pixelsX;
        }

        public int getPixelsY() {
            return pixelsY;
        }

        /**
         * Implement this interface to gain direct access to {@link Analysis} methods.
         */
        public interface Analyzable {

            Analysis getPixelAnalysis();

            default int getPixelsX() {
                return getPixelAnalysis().getPixelsX();
            }

            default int getPixelsY() {
                return getPixelAnalysis().getPixelsY();
            }

            default int getLowestPixel() {
                return getPixelAnalysis().getLowestPixel();
            }

            default int getHighestPixel() {
                return getPixelAnalysis().getHighestPixel();
            }

            default int getLeftmostPixel() {
                return getPixelAnalysis().getLeftmostPixel();
            }

            default int getRightmostPixel() {
                return getPixelAnalysis().getRightmostPixel();
            }

            default float getPixelMassCenterX() {
                return getPixelAnalysis().getPixelMassCenterX();
            }

            default float getPixelMassCenterY() {
                return getPixelAnalysis().getPixelMassCenterY();
            }

            default int getLowestPixelFromAbove() {
                return getPixelAnalysis().getLowestPixelFromAbove();
            }

            default int getHighestPixelFromBelow() {
                return getPixelAnalysis().getHighestPixelFromBelow();
            }

            default int getPixelHeight() {
                return getPixelAnalysis().getPixelHeight();
            }

            default int getPixelWidth() {
                return getPixelAnalysis().getPixelWidth();
            }

        }

    }
}
