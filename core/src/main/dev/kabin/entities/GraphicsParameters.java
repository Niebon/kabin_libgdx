package dev.kabin.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import org.jetbrains.annotations.NotNull;

/**
 * Parameters used for rendering.
 */
public interface GraphicsParameters {
    @NotNull
    SpriteBatch getBatch();
    float getStateTime();
    float getScreenWidth();
    float getScreenHeight();
    float getCamX();
    float getCamY();
    float getScale();
}
