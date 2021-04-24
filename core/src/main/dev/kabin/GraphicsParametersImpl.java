package dev.kabin;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import dev.kabin.entities.libgdximpl.EntityGroup;
import dev.kabin.entities.libgdximpl.EntityLibgdx;
import dev.kabin.entities.libgdximpl.GraphicsParametersLibgdx;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Consumer;

record GraphicsParametersImpl(@NotNull SpriteBatch batch,
                              @NotNull Camera camera,
                              @NotNull Consumer<@NotNull Consumer<@NotNull EntityLibgdx>> forEachEntityInCameraNeighborhood,
                              float timeElapsedSinceLastFrame,
                              float scale,
                              float screenWidth,
                              float screenHeight,
                              Map<EntityGroup, ShaderProgram> shaders) implements GraphicsParametersLibgdx {


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
