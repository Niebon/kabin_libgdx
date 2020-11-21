package dev.kabin.utilities.helperinterfaces;

/**
 * Slap this on entities which have scale. Scale = 1 corresponds to the pixel art resolution
 * of this project which is 400 x 255. Any deviation from this, (e.g. while viewing on a 1920 x 1080 display)
 * leads to a scale factor determined by a width quotient.
 */
public interface Scalable {

    float getScale();
    void setScale(float scale);

}
