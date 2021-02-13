package dev.kabin.physics;

import dev.kabin.GlobalData;
import dev.kabin.entities.Entity;
import dev.kabin.entities.EntityCollectionProvider;
import dev.kabin.utilities.functioninterfaces.BiIntPredicate;
import dev.kabin.utilities.functioninterfaces.BiIntToFloatFunction;

/**
 * In order to make rendering deterministic,
 * use a standard dt of 60 fps.
 */
public class PhysicsEngine {

    public final static float DT = 1 / 60f;
    public static float meter = 12 * GlobalData.scaleFactor;
    public static float gravitationConstant = 9.81f * meter;
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

    public static void render(float stateTime, Entity.PhysicsParameters params) {
        int numberOfFramesToRender = findNumberOfFramesToRender(stateTime);
        for (int i = 0; i < numberOfFramesToRender; i++) {
            renderFrame(params);
        }
    }

    static void renderFrame(Entity.PhysicsParameters params) {
        EntityCollectionProvider.actionForEachEntityOrderedByType(e -> e.updatePhysics(params));
    }

}
