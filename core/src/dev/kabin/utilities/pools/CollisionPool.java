package dev.kabin.utilities.pools;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import dev.kabin.geometry.points.ImmutablePointInt;
import dev.kabin.geometry.points.PrimitivePointInt;
import dev.kabin.global.GlobalData;
import dev.kabin.utilities.Functions;
import dev.kabin.utilities.functioninterfaces.IntPrimitivePairPredicate;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.function.BiFunction;

public class CollisionPool {

	// TODO: define or import int to object hash maps.
	// References to collision points for image pool.
	private static final Map<String, Map<Integer, List<PrimitivePointInt>>> pathIndexPairToCollisionProfile = new HashMap<>();
	private static final Map<String, Map<Integer, List<PrimitivePointInt>>> pathIndexPairToCollisionProfileBoundary = new HashMap<>();
	private static final Map<String, Map<Integer, List<PrimitivePointInt>>> pathIndexPairToSurfaceContourMapping = new HashMap<>();
	private static final Map<String, Map<Integer, IntPrimitivePairPredicate>> pathIndexPairToCollisionCheck = new HashMap<>();


	public static IntPrimitivePairPredicate findCollisionCheck(String path, int index) {

		if (!pathIndexPairToCollisionCheck.containsKey(path)) {
			pathIndexPairToCollisionCheck.put(path, new HashMap<>());
		}

		return pathIndexPairToCollisionCheck.get(path).computeIfAbsent(index, missing -> {

			final List<PrimitivePointInt> profile = findCollisionProfile(path, index);
			final int maxX = profile.stream().mapToInt(PrimitivePointInt::getX).max().orElse(0);
			final int maxY = profile.stream().mapToInt(PrimitivePointInt::getY).max().orElse(0);
			final boolean[][] predicateHelper = new boolean[maxX + 1][maxY + 1];
			for (boolean[] booleans : predicateHelper) {
				Arrays.fill(booleans, false);
			}
			for (PrimitivePointInt p : profile) {
				predicateHelper[p.getX()][p.getY()] = true;
			}
			return (i, j) -> predicateHelper[i][j];
		});
	}

    public static List<PrimitivePointInt> findCollisionProfile(String path, int index) {
        if (!pathIndexPairToCollisionProfile.containsKey(path) || pathIndexPairToCollisionProfile.get(path).containsKey(index)) {
            calculateCollisionData(path, index);
        }
        return pathIndexPairToCollisionProfile.get(path).get(index);
    }


	public static List<PrimitivePointInt> findSurfaceContourProfile(String path, int index) {
		if (!pathIndexPairToSurfaceContourMapping.containsKey(path) || pathIndexPairToSurfaceContourMapping.get(path).containsKey(index)) {
			calculateCollisionData(path, index);
		}
		return pathIndexPairToSurfaceContourMapping.get(path).get(index);
	}


	private static void calculateCollisionData(
			String path,
			int index
	) {

		final int width, height;
		final TextureAtlas.AtlasRegion atlasRegion = Arrays.stream(GlobalData.getAtlas().getRegions().items).filter(
				a -> path.equals(a.getTexture().toString()) && index == a.index
		).findFirst().orElseThrow();
		int x = atlasRegion.getRegionX();
		int y = atlasRegion.getRegionY();
		width = atlasRegion.getRegionWidth();
		height = atlasRegion.getRegionHeight();


		final BufferedImage bufferedImage = ImagePool.findBufferedImage(GlobalData.TEXTURES_PATH);

		// proc: add map entries
		{
			pathIndexPairToCollisionProfile.putIfAbsent(path, new HashMap<>());
			pathIndexPairToCollisionProfile.get(path).put(index, new ArrayList<>());
			pathIndexPairToCollisionProfileBoundary.putIfAbsent(path, new HashMap<>());
			pathIndexPairToCollisionProfileBoundary.get(path).put(index, new ArrayList<>());
			pathIndexPairToSurfaceContourMapping.putIfAbsent(path, new HashMap<>());
		}

		// Helper function to do index validations.
		final BiFunction<Integer, Integer, Boolean>
				indexValidator = Functions.indexValidator(x, x + width, y, y + height);


        /*
        Here i : position x relative to image
             j : position y relative to image
         */
		for (int i = x; i < x + width; i++) {
			for (int j = y; j < y + height; j++) {
				final double alphaValue = (0xFF & (bufferedImage.getRGB(i, j) >> 24));
				final boolean collision = (alphaValue > 0);
				final boolean pointOfPath;
				if (collision) {
					if (i == 0) {
						pointOfPath = true;
					} else {
						pointOfPath = indexValidator.apply(i, j - 1)
								&& !((0xFF & (bufferedImage.getRGB(i, j - 1) >> 24)) > 0);
					}
				} else pointOfPath = false;
				if (collision) {
					pathIndexPairToCollisionProfile.get(path).get(index).add(new ImmutablePointInt(i - x, j - y));
				}
				if (pointOfPath) {
					pathIndexPairToSurfaceContourMapping.get(path).get(index).add(new ImmutablePointInt(i - x, j - y));
				}
			}
		}

        /*
        Finally, calculate collision profile boundary.
        */
        pathIndexPairToCollisionProfileBoundary.get(path).put(index, findCollisionProfileBoundary(x, y, width, height, bufferedImage));


		// Finally, transform to game coordinates: positive y-direction points upwards.
        pathIndexPairToCollisionProfile.get(path).get(index).replaceAll(p -> new ImmutablePointInt(p.getX(), transformY(p.getY(), height)));
        pathIndexPairToCollisionProfileBoundary.get(path).get(index).replaceAll(p -> new ImmutablePointInt(p.getX(), transformY(p.getY(), height)));
        pathIndexPairToSurfaceContourMapping.get(path).get(index).replaceAll(p -> new ImmutablePointInt(p.getX(), transformY(p.getY(), height)));
	}

	/*
	A helper method for finding a convex representation of the collision profile boundary.
	*/
	private static @NotNull List<PrimitivePointInt> findCollisionProfileBoundary(int x, int y, int width, int height,
																				 BufferedImage bufferedImage) {

		List<PrimitivePointInt> collisionProfileBoundary = new ArrayList<>();

		outer:
		for (int i = x; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (boundaryFound(x + i, y + j, bufferedImage, collisionProfileBoundary)) continue outer;
			}
		}
		outer:
		for (int i = 0; i < width; i++) {
			for (int j = height - 1; j >= 0; j--) {
				if (boundaryFound(x + i, y + j, bufferedImage, collisionProfileBoundary)) continue outer;
			}
		}
		outer:
		for (int j = 0; j < height; j++) {
			for (int i = 0; i < width; i++) {
				if (boundaryFound(x + i, y + j, bufferedImage, collisionProfileBoundary)) continue outer;
			}
		}
		outer:
		for (int j = 0; j < height; j++) {
			for (int i = width - 1; i >= 0; i--) {
				if (boundaryFound(x + i, y + j, bufferedImage, collisionProfileBoundary)) continue outer;
			}
		}
		boolean keepLookingForPointsTooClose = true;
		while (keepLookingForPointsTooClose) {
			PrimitivePointInt toDiscard = null;
			looking:
			for (PrimitivePointInt p : collisionProfileBoundary) {
				for (PrimitivePointInt q : collisionProfileBoundary) {
					if (p != q && Functions.distance(p.getX(), p.getY(), q.getX(), q.getY()) < 1) {
						toDiscard = q;
						break looking;
					}
				}
			}
			if (toDiscard != null) collisionProfileBoundary.remove(toDiscard);
			else keepLookingForPointsTooClose = false;
		}

		return collisionProfileBoundary;
	}

	private static boolean boundaryFound(int i, int j, @NotNull BufferedImage bufferedImage, List<PrimitivePointInt> boundary) {
		final double alphaValue = (0xFF & (bufferedImage.getRGB(i, j) >> 24));
		final boolean collision = alphaValue > 0;
		if (collision) boundary.add(new ImmutablePointInt(i, j));
		return collision;
	}

	private static int transformY(int y, int height) {
	    return -y + height;
    }

}
