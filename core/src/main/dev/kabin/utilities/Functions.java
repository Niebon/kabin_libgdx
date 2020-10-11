package dev.kabin.utilities;

import dev.kabin.entities.Entity;
import dev.kabin.geometry.points.Point;
import dev.kabin.geometry.points.PointDouble;
import dev.kabin.geometry.points.PointInt;
import dev.kabin.geometry.shapes.AbstractRectBoxed;
import dev.kabin.geometry.shapes.RectBoxed;
import dev.kabin.global.GlobalData;
import dev.kabin.utilities.functioninterfaces.IntPrimitivePairPredicate;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * Class containing static methods which may be interpreted as mathematical function.
 */
public class Functions {

    /**
     * Parameters in {@link #intCircleCenteredAt(int, int, int)}
     */
    private static final int PIZZA_SLICE_DEGREES = 12;
    public static final int NUMBER_OF_SLICES = 360 / PIZZA_SLICE_DEGREES;

    @SuppressWarnings({"unused", "RedundantSuppression"})
    @Contract(pure = true)
    public static boolean oneTrue(@NotNull boolean... array) {
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0, size = array.length; i < size; i++) if (array[i]) return true;
        return false;
    }

    @Contract(pure = true)
    public static boolean oneFalse(@NotNull boolean... array) {
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0, size = array.length; i < size; i++) if (!array[i]) return true;
        return false;
    }

    @Contract(pure = true)
    public static boolean onePositive(@NotNull int... array) {
        for (int b : array) if (b > 0) return true;
        return false;
    }

    public static float snapToPixel(float x) {
        return Math.round(x / GlobalData.scaleFactor) * GlobalData.scaleFactor;
    }

    public static double distance(double x1, double y1, double x2, double y2) {
        double dx = x1 - x2;
        double dy = y1 - y2;
        return (Math.sqrt(dx * dx + dy * dy));
    }

    public static double distance(@NotNull RectBoxed<?> r1, @NotNull RectBoxed<?> r2) {
        return distance(r1.getCenterX(), r1.getMinY().doubleValue(), r2.getCenterX(), r2.getMinY().doubleValue());
    }

    public static double distance(@NotNull Entity entity1, @NotNull Entity entity2) {
        return distance(entity1.getX(), entity1.getY(), entity2.getX(), entity2.getY());
    }

    @NotNull
    @Contract("_, _ -> new")
    public static double[] getDisplacement(@NotNull Entity fromEntity, @NotNull Entity toEntity) {
        return new double[]{toEntity.getX() - fromEntity.getX(), toEntity.getY() - fromEntity.getY()};
    }

    @NotNull
    @Contract("_, _ -> new")
    public static double[] getDisplacement(@NotNull RectBoxed<?> from, @NotNull RectBoxed<?> to) {
        return new double[]{to.getCenterX() - from.getCenterX(), to.getMinY().doubleValue() - from.getMinY().doubleValue()};
    }

    public static double distance(@NotNull Entity entity, @NotNull AbstractRectBoxed<?> r) {
        return distance(entity.getX(), entity.getY(), r.getCenterX() * GlobalData.scaleFactor, r.getMinY().doubleValue() * GlobalData.scaleFactor);
    }

    public static double distance(@NotNull AbstractRectBoxed<?> r, @NotNull Entity entity) {
        return distance(entity.getX(), entity.getY(), r.getCenterX() * GlobalData.scaleFactor, r.getMinY().doubleValue() * GlobalData.scaleFactor);
    }

    public static double distance(double a, double b) {
        return Math.abs(a - b);
    }

    public static double distanceX(@NotNull Entity entity1, @NotNull Entity entity2) {
        return distance(entity1.getX(), entity2.getX());
    }

    public static double distanceY(@NotNull Entity entity1, @NotNull Entity entity2) {
        return distance(entity1.getY(), entity2.getY());
    }

    @Contract(pure = true)
    public static double distance(@NotNull PointInt p1, @NotNull PointInt p2) {
        return distance(p1.x, p1.y, p2.x, p2.y);
    }

    /**
     * Returns the distance from a point to the border of a rectangle.
     *
     * @param r   rectangle.
     * @param p   point.
     * @param <T> number.
     * @return distance.
     */
    public static <T extends Number & Comparable<T>> double distance(@NotNull AbstractRectBoxed<T> r, @NotNull Point<T> p) {
        double dx = Math.max(Math.abs(p.getX().doubleValue() - r.getCenterX()) - 0.5 * (r.getMaxX().doubleValue() - r.getMinX().doubleValue()), 0);
        double dy = Math.max(Math.abs(p.getY().doubleValue() - r.getCenterY()) - 0.5 * (r.getMaxY().doubleValue() - r.getMinY().doubleValue()), 0);
        return dx * dx + dy * dy;
    }

    public static double findAngleDeg(@NotNull PointDouble dr) {
        return findAngleDeg(dr.x(), dr.y());
    }

    /**
     * Returns the angle between [0, 360) of the given pair [dx,dy].
     *
     * @param dx horizontal displacement.
     * @param dy vertical displacement.
     * @return angle.
     */
    public static double findAngleDeg(double dx, double dy) {

        double angle = Math.round(Math.toDegrees(Math.atan(dy / dx)));

        if (dx < 0 && dy >= 0) angle = 180 + angle;      // 2nd quadrant
        else if (dx < 0 && dy <= 0) angle = 180 + angle; // 3nd quadrant
        else if (dx >= 0 && dy <= 0) angle = 360 + angle; // 4nd quadrant
        if (angle == 360) angle = 0;

        return angle;

    }

    /**
     * Assumes x and y correspond to points touching a walkable surface.
     * Returns an estimation of the normal vector at (x,y)
     *
     * @param x      coordinate
     * @param y      coordinate
     * @param radius parameter which determines the normal vector at (x,y)
     * @return estimate of normal vector
     */
    @NotNull
    @Contract("_, _, _, _ -> new")
    public static PointDouble normalVectorAt(int x, int y, int radius, IntPrimitivePairPredicate collisionPredicate) {
        int termsInSum = 0;
        double sumY = 0, sumX = 0;
        final int[][] circle = intCircleCenteredAt(x, y, radius);

        //noinspection ForLoopReplaceableByForEach
        for (int i = 0, circleSize = circle.length; i < circleSize; i++) {
            int x_ = circle[i][0];
            int y_ = circle[i][1];
            if (collisionPredicate.test(x_, y_)) {
                sumX = sumX + x_;
                sumY = sumY + y_;
                termsInSum++;
            }
        }
        final double
                avgY = sumY / termsInSum,
                avgX = sumX / termsInSum,
                dy = avgY - y,
                dx = avgX - x;
        double r = Math.sqrt(dx * dx + dy * dy);
        return Point.of(dx / r, dy / r);
    }

    /**
     * Finds a circle centered at (x,y) with given radius.
     *
     * @return a double int array with dimension [CIRCLE_RES][2].
     */
    @NotNull
    public static int[][] intCircleCenteredAt(int x, int y, int radius) {
        final int[][] circle = new int[NUMBER_OF_SLICES][2];
        for (int i = 0; i < NUMBER_OF_SLICES; i++) {
            circle[i][0] = (int) Math.round(radius * Math.cos(Math.toRadians(i * PIZZA_SLICE_DEGREES))) + x;
            circle[i][1] = (int) Math.round(radius * Math.sin(Math.toRadians(i * PIZZA_SLICE_DEGREES))) + y;
        }
        return circle;
    }


    /**
     * A linear reparametrization f: [0,1]->[min,max]
     *
     * @param x   argument, assumed to lie in [0,1]. If this is not the case, this function will not
     *            behave as expected.
     * @param min min output
     * @param max max output
     */
    public static double linearReparametrization(double x, double min, double max) {
        return min + x * (max - min);
    }


    public static boolean isBetweenDeg(int startDeg, int endDeg, int midDeg) {
        startDeg = Math.floorMod(startDeg, 360);
        endDeg = Math.floorMod(endDeg, 360);
        midDeg = Math.floorMod(midDeg, 360);
        if (endDeg < startDeg) endDeg = endDeg + 360;
        if (midDeg < startDeg) midDeg = midDeg + 360;
        return startDeg <= midDeg && midDeg <= endDeg;
    }


    /**
     * Hacky function which checks that a string is json formatted.
     */
    public static boolean validStringJson(String onTrial) {
        try {
            new JSONObject(onTrial);
            return true;
        } catch (JSONException jsonException) {
            System.out.printf("Got error '%s' while trying to parse json string '%s...'.%n", jsonException.getMessage(), onTrial.substring(0, 10));
            return false;
        }
    }

    @NotNull
    @Contract(pure = true)
    public static BiFunction<Integer, Integer, Boolean> indexValidator(
            final int minX,
            final int maxX,
            final int minY,
            final int maxY
    ) {
        return (x, y) -> minX <= x && x < maxX && minY <= y && y < maxY;
    }

    public static int toInt(double d, double scale) {
        return (int) Math.round(d / scale);
    }


    public static double clip(double x, double min, double max) {
        if (x < min) return min;
        return Math.min(x, max);
    }

    public static double sigmoid(double x) {
        if (x >= 0) {
            return 1 / (1 - Math.exp(x));
        } else {
            double ex = Math.exp(x);
            return ex / (1 + ex);
        }
    }

	/**
     * A transform from a coordinate system where y points downwards to one where y points upwards; also
     * it is translated by a "height" parameter.
     * <pre>
     *              ----> x                y
     *              | .p                   ^ .p
     *              |           ->         |
     *              v                      |____> x
     *              y
     * </pre>
     * For example {@link java.awt.image.BufferedImage BufferedImage}
     * uses the coordinate system to the left where the origin is the top left corner.
     * <p>
     * Other classes such as {@link com.badlogic.gdx.graphics.g2d.SpriteBatch SpriteBatch} and {@link com.badlogic.gdx.graphics.g2d.Sprite Sprite}
     * use the coordinate system to the right.
     * <p>
     * This function allows to transition from a <strong>TLC-coordinate system</strong> to a
     * <strong>BLC-coordinate system</strong>.
     *
     * @param y      coordinate.
     * @param height height of the given image.
     * @return new y-coordinate.
     */
    public static int transformY(int y, int height) {
        return -y + height;
    }

    /**
     * Similar as {@link #transformY(int y, int height)}, but the y parameter is a {@code float}.
     *
     * @param y      coordinate.
     * @param height height parameter.
     * @return new y-coordinate.
     */
    public static float transformY(float y, int height) {
        return -y + height;
    }


    public static float requireNonNullElse(float val, float defaultVal) {
        return val == 0 ? defaultVal : val;
    }

    public static <T> boolean anyTrue(T[] array, Predicate<T> predicate) {
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0, n = array.length; i < n; i++) {
            if (predicate.test(array[i])) {
                return true;
            }
        }
        return false;
    }
}
