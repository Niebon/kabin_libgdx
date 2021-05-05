package dev.kabin.util;

import dev.kabin.util.lambdas.BiIntPredicate;
import dev.kabin.util.points.PointFloat;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.Callable;


public class Functions {

    public static float snapToPixel(float input, float scaleFactor) {
        return Math.round(input / scaleFactor) * scaleFactor;
    }

    /**
     * Snaps the given value to the grid. More precisely, given the partition:
     * <p>
     * [a1,a2) u [a2,a2) u [a3,a3) u ...
     * <p>
     * where |ai-aj| = grid size if i - j = 1. If x is contained in the interval starting at ai, then x is snapped to
     * ai.
     *
     * @param value    the value x to be snapped.
     * @param gridSize the grid size.
     * @return the lower endpoint of the half open interval containing value from the partition determined by the grid.
     */
    public static int snapToGrid(float value, int gridSize) {
        return ((int) Math.floor(value / gridSize)) * gridSize;
    }

    public static double distance(double x1, double y1, double x2, double y2) {
        double dx = x1 - x2;
        double dy = y1 - y2;
        return (Math.sqrt(dx * dx + dy * dy));
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
    public static PointFloat normalVectorAt(int x, int y, int radius, BiIntPredicate collisionPredicate) {
        int termsInSum = 0;
        float sumY = 0, sumX = 0;
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
        final float
                avgY = sumY / termsInSum,
                avgX = sumX / termsInSum,
                dy = avgY - y,
                dx = avgX - x;
        float r = (float) Math.sqrt(dx * dx + dy * dy);
        return PointFloat.immutable(dx / r, dy / r);
    }

    /**
     * Finds a circle centered at (x,y) with given radius.
     *
     * @return a double int array with dimension [CIRCLE_RES][2].
     */
    public static int[][] intCircleCenteredAt(int x, int y, int radius) {

        class Params {
            static final int PIZZA_SLICE_DEGREES = 12;
            static final int NUMBER_OF_SLICES = 360 / PIZZA_SLICE_DEGREES;
        }

        final int[][] circle = new int[Params.NUMBER_OF_SLICES][2];
        for (int i = 0; i < Params.NUMBER_OF_SLICES; i++) {
            circle[i][0] = (int) Math.round(radius * Math.cos(Math.toRadians(i * Params.PIZZA_SLICE_DEGREES))) + x;
            circle[i][1] = (int) Math.round(radius * Math.sin(Math.toRadians(i * Params.PIZZA_SLICE_DEGREES))) + y;
        }
        return circle;
    }


    public static int toIntDivideBy(float d, float divideBy) {
        return Math.round(d / divideBy);
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

    public static float requireNonZeroElse(float val, float defaultVal) {
        return val == 0 ? defaultVal : val;
    }

    public static <T> T getNull() {
        return null;
    }

    public static <T> Optional<T> tryGet(Callable<T> callable) {
        try {
            return Optional.of(callable.call());
        } catch (Throwable t) {
            return Optional.empty();
        }
    }

}
