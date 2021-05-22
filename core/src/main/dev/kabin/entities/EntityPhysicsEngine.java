package dev.kabin.entities;

import java.util.function.Consumer;

/**
 * In order to make rendering deterministic,
 * use a standard dt of 120 fps.
 */
public class EntityPhysicsEngine<E extends Entity<?, ?, ?>> {

	// Constants
	public static final int METER = 16;
	public static final float DT_SECONDS = 1 / 60f;
	public static final float GRAVITATION_CONSTANT_PER_METER = 9.81f;

	// Private data
	private float accumulatedTimeElapsedSinceLastPhysicsFrame = 0f;

	private int findNumberOfPhysicsFramesToRender(float timeElapsedSinceLastPhysicsFrame) {
		if (timeElapsedSinceLastPhysicsFrame < DT_SECONDS) {
			return 0;
		} else {
			return 1 + findNumberOfPhysicsFramesToRender(timeElapsedSinceLastPhysicsFrame - DT_SECONDS);
		}
	}

	public void renderOutstandingPhysicsFrames(float timeElapsedSinceLastGraphicsFrame, PhysicsParameters params, Consumer<Consumer<E>> forEachEntity) {
		accumulatedTimeElapsedSinceLastPhysicsFrame += timeElapsedSinceLastGraphicsFrame;
//		int numberOfFramesToRender = findNumberOfPhysicsFramesToRender(accumulatedTimeElapsedSinceLastPhysicsFrame);
		if (accumulatedTimeElapsedSinceLastPhysicsFrame > DT_SECONDS) {
//			for (int i = 0; i < numberOfFramesToRender; i++) {
//				renderExactlyOneFrame(params, forEachEntity);
//			}
			renderExactlyOneFrame(params, forEachEntity);
			accumulatedTimeElapsedSinceLastPhysicsFrame = accumulatedTimeElapsedSinceLastPhysicsFrame - DT_SECONDS;
		}
	}

	public void renderExactlyOneFrame(PhysicsParameters params, Consumer<Consumer<E>> forEachEntity) {
		forEachEntity.accept(e -> e.updatePhysics(params));
	}

}
