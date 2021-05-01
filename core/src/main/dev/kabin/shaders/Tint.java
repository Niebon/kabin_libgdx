package dev.kabin.shaders;

import dev.kabin.util.helperinterfaces.JSONSerializable;
import org.json.JSONObject;

import java.util.Map;

/**
 * A <b>tint</b> is a triple of {@code float} values (red, green, blue) normalized to unit length as a vector -
 * ready to be multiplied by some ambient color in shader calculations.
 */
public class Tint implements JSONSerializable {

    private float red, green, blue;

    private Tint(float red, float green, float blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    /**
     * Creates a new instance of ting such that:
     * <ul>
     *     <li>If all provided coordinates are zero, then (r,g,b) = (0,0,0).</li>
     *     <li>Otherwise, the resulting (r,g,b) will be the result of scaling the input such that the brightest color = 1.</li>
     * </ul>
     *
     * @param red   the red value.
     * @param green the green value.
     * @param blue  the blue value.
     * @return a tint, ready to be multiplied by ambient color in shader calculations.
     */
    public static Tint of(float red, float green, float blue) {
        requireNonNegative(red, "red");
        requireNonNegative(green, "green");
        requireNonNegative(blue, "blue");
        float max = Math.max(red, Math.max(green, blue));
        return (max == 0)
                ? new Tint(0, 0, 0)
                : new Tint(red / max, green / max, blue / max);
    }

    /**
     * Helper method to throw exception in the factory method.
     *
     * @param value     the color value.
     * @param colorName the name of the color (referenced in the exception that is thrown).
     */
    private static void requireNonNegative(float value, String colorName) {
        if (value < 0) {
            throw new IllegalArgumentException(colorName + " was negative.");
        }
    }

    /**
     * Shit the color to the given coordinate. The input data will be normalized.
     *
     * @param red   the red value.
     * @param green the green value.
     * @param blue  the blue value.
     */
    public void set(float red, float green, float blue) {
        float r = (float) Math.sqrt(red * red + green * green + blue * blue);
        this.red = red / r;
        this.green = green / r;
        this.blue = blue / r;
    }

    public void setRed(float red) {
        this.red = red;
    }

    public void setGreen(float green) {
        this.green = green;
    }

    public void setBlue(float blue) {
        this.blue = blue;
    }

    /**
     * @return the red coordinate.
     */
    public float red() {
        return red;
    }

    /**
     * @return the green coordinate.
     */
    public float green() {
        return green;
    }

    /**
     * @return the blue coordinate.
     */
    public float blue() {
        return blue;
    }

    /**
     * A factory method for constructing an instance of {@link Tint} from json.
     *
     * @param o the object.
     * @return an instance matching the data.
     */
    public static Tint of(JSONObject o) {
        return Tint.of(o.getInt("r"), o.getInt("g"), o.getInt("b"));
    }

    @Override
    public JSONObject toJSONObject() {
        return new JSONObject(Map.of(
                "r", red,
                "g", green,
                "b", blue
        ));
    }
}
