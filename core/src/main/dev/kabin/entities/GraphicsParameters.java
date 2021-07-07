package dev.kabin.entities;

/**
 * Parameters used for rendering.
 */
public interface GraphicsParameters {

    float timeElapsedSinceLastFrame();

    float screenWidth();

    float screenHeight();

    float camX();

    float camY();

    float scaleX();

    float scaleY();

    float alpha();

    float red();

    float green();

    float blue();
}
