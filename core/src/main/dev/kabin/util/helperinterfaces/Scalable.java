package dev.kabin.util.helperinterfaces;

/**
 * Slap this on classes that have scale associated with their coordinates. Scale = 1 corresponds to the pixel art resolution
 * of this project which is 400 x 255. Any deviation from this, (e.g. viewing on a 1920 x 1080 display)
 * leads to a scale factor determined by a width quotient.
 */
public interface Scalable {

    /**
     * @return the scale associated with this instance.
     */
    float getScale();

    /**
     * Modifies this instance so that it behaves according to the new scale.
     * @param scale the new scale.
     */
    void setScale(float scale);

}
