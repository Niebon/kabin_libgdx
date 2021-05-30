package dev.kabin.entities;

import dev.kabin.util.events.KeyCode;

/**
 * Parameters for entities that undergo physical interactions with their surroundings.
 */
public interface PhysicsParameters {

    // Constants
    @SuppressWarnings("unused")
    float FPS_30 = 1 / 30f;
    float FPS_60 = 1 / 60f;
    @SuppressWarnings("unused")
    float FPS_120 = 1 / 120f;
    float GRAVITATION_CONSTANT_PER_METER = 9.81f;
    int METER = 16;

    /**
     * A collision check.
     *
     * @param x horizontal coordinate. Positive points right relative to the screen.
     * @param y vertical coordinate. Positive points upwards the screen.
     * @return true if the coordinate has collision.
     */
    boolean isCollisionAt(int x, int y);

	/**
	 * A ladder data check.
	 *
	 * @param x horizontal coordinate. Positive points right relative to the screen.
	 * @param y vertical coordinate. Positive points upwards the screen.
	 * @return true if the coordinate has ladder data.
	 */
    boolean isLadderAt(int x, int y);


    default boolean isCollisionIfNotLadderData(int x, int y) {
        if (isLadderAt(x, y)) return false;
        else return (isCollisionAt(x, y));
    }

    /**
     * Horizontal vector field.
     *
     * @param x horizontal coordinate. Positive points right relative to the screen.
     * @param y vertical coordinate. Positive points upwards the screen.
     * @return the magnitude of the vector field in horizontal direction.
     */
    float getVectorFieldX(int x, int y);

    /**
     * Vertical vector field.
     *
     * @param x horizontal coordinate. Positive points right relative to the screen.
     * @param y vertical coordinate. Positive points upwards the screen.
     * @return the magnitude of the vector field in vertical direction.
     */
    float getVectorFieldY(int x, int y);


    /**
     * Check if the given key is pressed. Use case: player movement.
     *
     * @param keycode which key.
     * @return true or false.
     */
    boolean isPressed(KeyCode keycode);


    /**
     * @return time step.
     */
    default float dt() {
        return FPS_30;
    }

    /**
     * @return a meter measured in pixels.
     */
    default float meter() {
        return METER;
    }
}
