package dev.kabin.utilities.pools;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import dev.kabin.geometry.points.Point;
import dev.kabin.geometry.points.PointInt;
import dev.kabin.global.GlobalData;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.util.*;

import static dev.kabin.global.GlobalData.TEXTURES_PATH;

public class ImageAnalysisPool {

	private static final Map<String, Map<Integer, Analysis>> data = new HashMap<>();

	public static void analyse(@NotNull String path, int index) {

	}

	public static Analysis findAnalysis(String path, int index) {
		if (!data.containsKey(path) || !data.get(path).containsKey(index)) {
			data.putIfAbsent(path, new HashMap<>());
			data.get(path).put(index, new Analysis(path, index));
		}
		return data.get(path).get(index);
	}


	public static class Analysis {

		final List<PointInt> pixelProfile = new ArrayList<>();
		double
				pixelMassCenterX,
				pixelMassCenterY;
		private int
				lowestPixel,
				highestPixel,
				leftmostPixel,
				rightmostPixel,
				lowestPixelFromAbove,
				highestPixelFromBelow, //TODO find this
				pixelHeight,
				pixelWidth,
				pixelMassCenterXInt,
				pixelMassCenterYInt,
				pixelsX, pixelsY;

		public Analysis(@NotNull String path, int index) {
			final int width, height;
			final TextureAtlas.AtlasRegion atlasRegion = Arrays.stream(GlobalData.getAtlas().getRegions().items).filter(
					a -> path.equals(a.getTexture().toString()) && index == a.index
			).findFirst().orElseThrow();
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
						pixelProfile.add(Point.of(i, j));
					}
				}
			}

			// Find lowest pixel from above.
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

			this.lowestPixel = lowestPixel;
			this.highestPixel = highestPixel;
			this.rightmostPixel = rightmostPixel;
			this.leftmostPixel = leftmostPixel;

			pixelMassCenterX = (double) sumx / nx;
			pixelMassCenterY = (double) sumy / ny;
			pixelMassCenterXInt = (int) Math.round(pixelMassCenterX);
			pixelMassCenterYInt = (int) Math.round(pixelMassCenterY);

			pixelHeight = lowestPixel - highestPixel;
			pixelWidth = rightmostPixel - leftmostPixel;
			pixelsX = width;
			pixelsY = height;
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

		public double getPixelMassCenterX() {
			return pixelMassCenterX;
		}

		public double getPixelMassCenterY() {
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

			default double getPixelMassCenterX() {
				return getPixelAnalysis().getPixelMassCenterX();
			}

			default double getPixelMassCenterY() {
				return getPixelAnalysis().getPixelMassCenterY();
			}

			default int getLowestPixelFromAbove() {
				return getPixelAnalysis().getLowestPixelFromAbove();
			}

			// I loathe the lack of symmetry more than this variable not being in use (for the moment at least.)
			@SuppressWarnings("unused")
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
