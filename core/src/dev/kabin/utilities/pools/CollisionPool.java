package dev.kabin.utilities.pools;

import dev.kabin.geometry.points.PointInt;
import dev.kabin.utilities.Functions;
import dev.kabin.utilities.functioninterfaces.IntPrimitivePairPredicate;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.function.BiFunction;

public class CollisionPool {

	// TODO: define or import int to object hash maps.
	// References to collision points for image pool.
	private static final Map<Integer, Map<Integer, List<PointInt>>> indexPairToCollisionProfile = new HashMap<>();
	private static final Map<Integer, Map<Integer, List<PointInt>>> indexPairToCollisionProfileBoundary = new HashMap<>();
	private static final Map<Integer, Map<Integer, List<PointInt>>> indexPairToSurfaceContourMapping = new HashMap<>();
	private static final Map<Integer, Map<Integer, IntPrimitivePairPredicate>> indexPairToCollisionCheck = new HashMap<>();
	private static final String TEXTURES_PATH = "core/assets/textures.png";


	public static IntPrimitivePairPredicate findCollisionCheck(int x, int y) {

		if (!indexPairToCollisionCheck.containsKey(x)) {
			indexPairToCollisionCheck.put(x, new HashMap<>());
		}

		return indexPairToCollisionCheck.get(x).computeIfAbsent(y, missing -> {
			final List<PointInt> profile = indexPairToCollisionProfile.get(x).get(y);
			final int maxX = profile.stream().mapToInt(PointInt::x).max().orElse(0);
			final int maxY = profile.stream().mapToInt(PointInt::y).max().orElse(0);
			final boolean[][] predicateHelper = new boolean[maxX + 1][maxY + 1];
			for (boolean[] booleans : predicateHelper) {
				Arrays.fill(booleans, false);
			}
			for (PointInt p : profile) {
				predicateHelper[p.x][p.y] = true;
			}
			return (i, j) -> predicateHelper[i][j];
		});
	}

//    private static List<PointInt> findCollisionProfile(int minX, int minY) {
//        if (!imagePathToCollisionProfileMapping.containsKey(genKey(minX, minY))) {
//            calculateCollisionData(minX, minY);
//        }
//        return imagePathToCollisionProfileMapping.get(genKey(minX, minY));
//    }

	private static void calculateCollisionData(
			int x,
			int y,
			int width,
			int height
	) {

		final BufferedImage bufferedImage;


		bufferedImage = ImagePool.findBufferedImage(TEXTURES_PATH);


		// proc: add map entries
		{
			indexPairToCollisionProfile.putIfAbsent(x, new HashMap<>());
			indexPairToCollisionProfile.get(x).put(y, new ArrayList<>());
			indexPairToCollisionProfileBoundary.putIfAbsent(x, new HashMap<>());
			indexPairToCollisionProfileBoundary.get(x).put(y, new ArrayList<>());
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
					indexPairToCollisionProfile.get(x).get(y).add(new PointInt(i - x, j - y));
				}
				if (pointOfPath) {
					indexPairToSurfaceContourMapping.get(x).get(y).add(new PointInt(i - x, j - y));
				}
			}
		}

        /*
        Finally, calculate collision profile boundary.
        */
		indexPairToSurfaceContourMapping.putIfAbsent(x,
				new HashMap<>(Map.of(y, findCollisionProfileBoundary(x, y, width, height, bufferedImage)))
		);
	}

	/*
	A helper method for finding a convex representation of the collision profile boundary.
	*/
	private static @NotNull List<PointInt> findCollisionProfileBoundary(int x, int y, int width, int height,
																		BufferedImage bufferedImage) {

		List<PointInt> collisionProfileBoundary = new ArrayList<>();

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
			PointInt toDiscard = null;
			looking:
			for (PointInt p : collisionProfileBoundary) {
				for (PointInt q : collisionProfileBoundary) {
					if (p != q && Functions.distance(p.x, p.y, q.x, q.y) < 1) {
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

	private static boolean boundaryFound(int i, int j, @NotNull BufferedImage bufferedImage, List<PointInt> boundary) {
		final double alphaValue = (0xFF & (bufferedImage.getRGB(i, j) >> 24));
		final boolean collision = alphaValue > 0;
		if (collision) boundary.add(new PointInt(i, j));
		return collision;
	}


}
