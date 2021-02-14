package dev.kabin.utilities.pools;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import dev.kabin.GlobalData;
import dev.kabin.utilities.Functions;
import dev.kabin.utilities.functioninterfaces.BiIntPredicate;
import dev.kabin.utilities.points.PointInt;
import dev.kabin.utilities.points.UnmodifiablePointInt;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.util.*;

public class CollisionPool {

    // TODO: impl or import int to object maps.
    // References to collision points for image pool.
    private static final Map<String, Map<Integer, List<PointInt>>> pathIndexPairToCollisionProfile = new HashMap<>();
    private static final Map<String, Map<Integer, List<PointInt>>> pathIndexPairToCollisionProfileBoundary = new HashMap<>();
    private static final Map<String, Map<Integer, List<PointInt>>> pathIndexPairToSurfaceContourMapping = new HashMap<>();
    private static final Map<String, Map<Integer, BiIntPredicate>> pathIndexPairToCollisionCheck = new HashMap<>();


    public static BiIntPredicate findCollisionCheck(String path, int index) {

        if (!pathIndexPairToCollisionCheck.containsKey(path)) {
            pathIndexPairToCollisionCheck.put(path, new HashMap<>());
        }

        return pathIndexPairToCollisionCheck.get(path).computeIfAbsent(index, missing -> {

            final List<PointInt> profile = findCollisionProfile(path, index);
            final int maxX = profile.stream().mapToInt(PointInt::x).max().orElse(0);
            final int maxY = profile.stream().mapToInt(PointInt::y).max().orElse(0);
            final boolean[][] predicateHelper = new boolean[maxX + 1][maxY + 1];
            for (boolean[] booleans : predicateHelper) {
                Arrays.fill(booleans, false);
            }
            for (PointInt p : profile) {
                predicateHelper[p.x()][p.y()] = true;
            }
            return (i, j) -> predicateHelper[i][j];
        });
    }


    public static List<PointInt> findCollisionProfile(String path, int index) {
        if (!pathIndexPairToCollisionProfile.containsKey(path) || !pathIndexPairToCollisionProfile.get(path).containsKey(index)) {
            calculateCollisionData(path, index);
        }
        return pathIndexPairToCollisionProfile.get(path).get(index);
    }


    public static List<PointInt> findSurfaceContourProfile(String path, int index) {
        if (!pathIndexPairToSurfaceContourMapping.containsKey(path) || !pathIndexPairToSurfaceContourMapping.get(path).containsKey(index)) {
            calculateCollisionData(path, index);
        }
        return pathIndexPairToSurfaceContourMapping.get(path).get(index);
    }


    private static void calculateCollisionData(
            String path,
            int index
    ) {

        final TextureRegion atlasRegion = GlobalData.getAtlas().findRegion(path, index);

        if (atlasRegion == null) {
        	throw new RuntimeException("No atlas region matching name == " + path + ", index == " + index + ".");
		}

        final int x = atlasRegion.getRegionX();
        final int y = atlasRegion.getRegionY();
        final int width = atlasRegion.getRegionWidth();
        final int height = atlasRegion.getRegionHeight();


        final BufferedImage bufferedImage = ImagePool.findBufferedImage(GlobalData.TEXTURES_PATH);

        // proc: add map entries
        {
            pathIndexPairToCollisionProfile.putIfAbsent(path, new HashMap<>());
            pathIndexPairToCollisionProfile.get(path).put(index, new ArrayList<>());

            pathIndexPairToCollisionProfileBoundary.putIfAbsent(path, new HashMap<>());
            pathIndexPairToCollisionProfileBoundary.get(path).put(index, new ArrayList<>());

            pathIndexPairToSurfaceContourMapping.putIfAbsent(path, new HashMap<>());
            pathIndexPairToSurfaceContourMapping.get(path).put(index, new ArrayList<>());
        }

        // Helper function to do index validations.
        final BiIntPredicate indexValidator = Functions.indexValidator(x, x + width, y, y + height);


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
                        pointOfPath = indexValidator.test(i, j - 1)
                                && !((0xFF & (bufferedImage.getRGB(i, j - 1) >> 24)) > 0);
                    }
                } else pointOfPath = false;
                if (collision) {
                    pathIndexPairToCollisionProfile.get(path).get(index).add(new UnmodifiablePointInt(i - x, j - y));
                }
                if (pointOfPath) {
                    pathIndexPairToSurfaceContourMapping.get(path).get(index).add(new UnmodifiablePointInt(i - x, j - y));
                }
            }
        }

        /*
        Finally, calculate collision profile boundary.
        */
        pathIndexPairToCollisionProfileBoundary.get(path).put(index, findCollisionProfileBoundary(x, y, width, height, bufferedImage));

        // Finally, transform to game coordinates: positive y-direction points upwards ...
        pathIndexPairToCollisionProfile.get(path).get(index).replaceAll(p -> new UnmodifiablePointInt(p.x(), Functions.transformY(p.y(), height)));
        pathIndexPairToCollisionProfileBoundary.get(path).get(index).replaceAll(p -> new UnmodifiablePointInt(p.x(), Functions.transformY(p.y(), height)));
        pathIndexPairToSurfaceContourMapping.get(path).get(index).replaceAll(p -> new UnmodifiablePointInt(p.x(), Functions.transformY(p.y(), height)));


        // ... and make collections unmodifiable:
        {
            pathIndexPairToCollisionProfile.get(path)
                    .put(index, Collections.unmodifiableList(pathIndexPairToCollisionProfile.get(path).get(index)));
            pathIndexPairToCollisionProfileBoundary.get(path)
                    .put(index, Collections.unmodifiableList(pathIndexPairToCollisionProfileBoundary.get(path).get(index)));
            pathIndexPairToSurfaceContourMapping.get(path)
                    .put(index, Collections.unmodifiableList(pathIndexPairToSurfaceContourMapping.get(path).get(index)));
        }
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
                    if (p != q && Functions.distance(p.x(), p.y(), q.x(), q.y()) < 1) {
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
        if (collision) boundary.add(new UnmodifiablePointInt(i, j));
        return collision;
    }

}
