package dev.kabin.physics;

import dev.kabin.entities.PhysicsParameters;
import dev.kabin.entities.impl.EntityLibgdx;

import java.util.function.Consumer;

/**
 * In order to make rendering deterministic,
 * use a standard dt of 120 fps.
 */
public class PhysicsEngine {

	public static final int METER = 10; // One meter in pixels
	public static final float DT = 1 / 120f;
	public static final float GRAVITATION_CONSTANT = 9.81f;
	private static float lastFrame = 0f;

	private static int findNumberOfFramesToRender(float stateTime) {
		int frames = 1;
		float timeElapsedSinceLastFrame = stateTime - lastFrame;
		while (frames * DT < timeElapsedSinceLastFrame) {
			frames++;
		}
		lastFrame = stateTime;
		return frames - 1;
	}

	public static void renderOutstandingFrames(float stateTime, PhysicsParameters params, Consumer<Consumer<EntityLibgdx>> forEachEntity) {
		int numberOfFramesToRender = findNumberOfFramesToRender(stateTime);
		for (int i = 0; i < numberOfFramesToRender; i++) {
			renderExactlyOneFrame(params, forEachEntity);
		}
	}

	public static void renderExactlyOneFrame(PhysicsParameters params, Consumer<Consumer<EntityLibgdx>> forEachEntity) {
		forEachEntity.accept(e -> e.updatePhysics(params));
	}

}
