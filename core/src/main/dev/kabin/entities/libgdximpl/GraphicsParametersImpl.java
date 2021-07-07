package dev.kabin.entities.libgdximpl;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Map;
import java.util.function.Consumer;

public record GraphicsParametersImpl(@NotNull SpriteBatch batch,
                                     @NotNull Camera camera,
                                     @NotNull Consumer<@NotNull Consumer<@NotNull EntityLibgdx>> forEachEntityInCameraNeighborhood,
                                     float timeElapsedSinceLastFrame,
                                     float scaleX,
                                     float scaleY,
                                     float red,
                                     float green,
                                     float blue,
                                     float alpha,
                                     float screenWidth,
                                     float screenHeight,
                                     @UnmodifiableView Map<EntityGroup, ShaderProgram> shaders) implements GraphicsParametersLibgdx {

    public static GraphicsParametersImpl of(@NotNull SpriteBatch batch,
                                            @NotNull Camera camera,
                                            @NotNull Consumer<@NotNull Consumer<@NotNull EntityLibgdx>> forEachEntityInCameraNeighborhood,
                                            float timeElapsedSinceLastFrame,
                                            float scaleX,
                                            float scaleY,
                                            float screenWidth,
                                            float screenHeight,
                                            @UnmodifiableView Map<EntityGroup, ShaderProgram> shaders) {
        return new GraphicsParametersImpl(
                batch,
                camera,
                forEachEntityInCameraNeighborhood,
                timeElapsedSinceLastFrame,
                scaleX,
                scaleY,
                1, 1, 1, 1,
                screenWidth,
                screenHeight,
                shaders
        );
    }


    @Override
    public float camX() {
        return camera.position.x;
    }

    @Override
    public float camY() {
        return camera.position.y;
    }

    @Override
    public @Nullable ShaderProgram shaderFor(EntityGroup group) {
        return shaders.get(group);
    }
}
