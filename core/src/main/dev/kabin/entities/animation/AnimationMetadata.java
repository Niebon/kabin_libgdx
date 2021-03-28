package dev.kabin.entities.animation;

import dev.kabin.util.Direction;

/**
 * Metadata associated with an animation enum constant.
 */
public interface AnimationMetadata {

    /**
     * @return true iff this animation is looping.
     */
    boolean isLooping();

    /**
     * @return the direction of this animation.
     */
    Direction getDirection();

    /**
     * @return true iff the last frame of this animation is repeating.
     */
    boolean isLastFrameRepeating();

}
