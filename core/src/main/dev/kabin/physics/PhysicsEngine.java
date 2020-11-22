package dev.kabin.physics;

import dev.kabin.entities.Entity;
import dev.kabin.entities.EntityGroupProvider;
import dev.kabin.GlobalData;

/**
 * In order to make rendering deterministic,
 * use a standard dt of 60 fps.
 */
public class PhysicsEngine {

	private final static float DT = 1 / 60f;
	private static float lastFrame = 0f;

	private static int findNumberOfFramesToRender(float stateTime) {
		int frames = 1;
		float timeElapsedSinceLastFrame = stateTime - lastFrame;
		while (frames * DT < timeElapsedSinceLastFrame) {
			frames++;
		}
		lastFrame = GlobalData.stateTime;
		return frames - 1;
	}

	public static void render(float stateTime) {
		int numberOfFramesToRender = findNumberOfFramesToRender(stateTime);
		for (int i = 0; i < numberOfFramesToRender; i++) {
			renderFrame();
		}
	}

	static void renderFrame() {
		EntityGroupProvider.actionForEachEntityOrderedByGroup(Entity::updatePhysics);
	}

}
