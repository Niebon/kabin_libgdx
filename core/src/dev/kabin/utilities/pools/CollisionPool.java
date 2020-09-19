package dev.kabin.utilities.pools;

import dev.kabin.geometry.points.PointInt;
import dev.kabin.utilities.Functions;
import dev.kabin.utilities.functioninterfaces.IntPrimitivePairPredicate;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.function.BiFunction;

public class CollisionPool {

    // References to collision points for image pool.
    private static final Map<String, List<PointInt>> imagePathToCollisionProfileMapping = new HashMap<>();
    private static final Map<String, List<PointInt>> imagePathToCollisionProfileBoundaryMapping = new HashMap<>();
    private static final Map<String, List<PointInt>> imagePathToSurfaceContourMapping = new HashMap<>();
    private static final Map<String, IntPrimitivePairPredicate> collisionCheck = new HashMap<>();


    public static IntPrimitivePairPredicate findCollisionCheck(String imagePath, String frameKey) {
        return collisionCheck.computeIfAbsent(imagePath + frameKey, missing -> {
            final List<PointInt> profile = findCollisionProfile(imagePath, frameKey);
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

    private static String generatePath(String imageAssetFolder, String filenameNoExtension) {
        return imageAssetFolder + '/' + filenameNoExtension + ".png";
    }

    public static List<PointInt> findCollisionProfile(String imageAssetFolder, String filenameNoExtension) {
        final String path = generatePath(imageAssetFolder, filenameNoExtension);
        if (!imagePathToCollisionProfileMapping.containsKey(imageAssetFolder + filenameNoExtension)) {
            calculateCollisionData(path);
        }
        return imagePathToCollisionProfileMapping.get(imageAssetFolder + filenameNoExtension);
    }

    public static List<PointInt> findCollisionProfileBoundary(String imageAssetFolder, String filenameNoExtension) {
        final String path = generatePath(imageAssetFolder, filenameNoExtension);
        if (!imagePathToCollisionProfileBoundaryMapping.containsKey(path)) {
            calculateCollisionData(path);
        }
        return imagePathToCollisionProfileBoundaryMapping.get(path);
    }

    public static List<PointInt> findSurfaceContourProfile(String imageAssetFolder, String filenameNoExtension) {
        final String path = generatePath(imageAssetFolder, filenameNoExtension);
        if (!imagePathToSurfaceContourMapping.containsKey(path)) {
            calculateCollisionData(path);
        }
        return imagePathToSurfaceContourMapping.get(path);
    }

    private static void calculateCollisionData(@NotNull String imageResource) {

        final BufferedImage bufferedImage;
        final int minX, minY, width, height;

        if (imageResource.endsWith(".png")) {
            bufferedImage = ImagePool.findBufferedImage(imageResource);
            width = bufferedImage.getWidth();
            height = bufferedImage.getHeight();
            minX = 0;
            minY = 0;
        } else throw new RuntimeException("Provided image resource was not '.png'.");

        final BiFunction<Integer, Integer, Boolean>
                indexValidator = Functions.indexValidator(minX, minX + width, minY, minY + height);

        imagePathToCollisionProfileMapping.put(imageResource, new ArrayList<>());
        imagePathToSurfaceContourMapping.put(imageResource, new ArrayList<>());
        /*
        Here i : position x relative to image
             j : position y relative to image
         */
        for (int i = minX; i < minX + width; i++) {
            for (int j = minY; j < minY + height; j++) {
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
                    imagePathToCollisionProfileMapping.get(imageResource).add(new PointInt(i - minX, j - minY));
                }
                if (pointOfPath) {
                    imagePathToSurfaceContourMapping.get(imageResource).add(new PointInt(i - minX, j - minY));
                }
            }
        }

        /*
        Calculate collision profile boundary.
        */
        imagePathToCollisionProfileBoundaryMapping.put(imageResource,
                findCollisionProfileBoundary(minX, minY, width, height, bufferedImage));
    }

    /*
    A helper method for finding a convex representation of the collision profile boundary.
    */
    private static @NotNull List<PointInt> findCollisionProfileBoundary(int minX, int minY, int width, int height,
                                                                        BufferedImage bufferedImage) {

        List<PointInt> collisionProfileBoundary = new ArrayList<>();

        outer:
        for (int i = minX; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (boundaryFound(minX + i, minY + j, bufferedImage, collisionProfileBoundary)) continue outer;
            }
        }
        outer:
        for (int i = 0; i < width; i++) {
            for (int j = height - 1; j >= 0; j--) {
                if (boundaryFound(minX + i, minY + j, bufferedImage, collisionProfileBoundary)) continue outer;
            }
        }
        outer:
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                if (boundaryFound(minX + i, minY + j, bufferedImage, collisionProfileBoundary)) continue outer;
            }
        }
        outer:
        for (int j = 0; j < height; j++) {
            for (int i = width - 1; i >= 0; i--) {
                if (boundaryFound(minX + i, minY + j, bufferedImage, collisionProfileBoundary)) continue outer;
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
