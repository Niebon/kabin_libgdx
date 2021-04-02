package dev.kabin.entities.impl;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import dev.kabin.entities.GraphicsParameters;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Graphics parameters for libgdx implementations.
 */
public interface GraphicsParametersLibgdx extends GraphicsParameters {

    /**
     * @return the sprite batch on which to draw.
     * @see SpriteBatch#draw(Texture, float, float)
     */
    @NotNull
    SpriteBatch getBatch();

    /**
     * @return a consumer that takes an action to be run for each entity, and runs this action for each
     * entity that is visible in the current camera neighborhood.
     */
    Consumer<Consumer<EntityLibgdx>> forEachEntityInCameraNeighborhood();

}
