package dev.kabin.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import dev.kabin.entities.impl.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

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

	Consumer<Consumer<Entity>> forEachEntityInCameraNeighborhood();
}
