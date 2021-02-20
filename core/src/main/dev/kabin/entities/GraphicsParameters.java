package dev.kabin.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Parameters used for rendering.
 */
public interface GraphicsParameters {
    SpriteBatch getBatch();
    float getStateTime();
    float getScreenWidth();
    float getScreenHeight();
    float getCamX();
    float getCamY();
    float getScale();
}
