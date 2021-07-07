package dev.kabin.entities.libgdximpl;

import dev.kabin.entities.AnimationPlayback;

/**
 * All animation playbacks used for the libgdx implementation should extend this interface.
 *
 * @param <AnimationType> a set of animations.
 */
public interface AnimationPlaybackLibgdx<AnimationType extends Enum<AnimationType>> extends
        AnimationPlayback<AnimationType, GraphicsParametersLibgdx> {
}
