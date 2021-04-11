package dev.kabin.entities.libgdximpl;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import dev.kabin.entities.GraphicsParameters;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    SpriteBatch batch();

    /**
     * @return a consumer that takes an action to be run for each entity, and runs this action for each
     * entity that is visible in the current camera neighborhood.
     */
    @NotNull Consumer<@NotNull Consumer<@NotNull EntityLibgdx>> forEachEntityInCameraNeighborhood();

    /**
     * A reference to the shader program to be used for a given entity group.
     *
     * @param group which group.
     * @return the shader.
     */
    @Nullable
    ShaderProgram shaderFor(EntityGroup group);
}
