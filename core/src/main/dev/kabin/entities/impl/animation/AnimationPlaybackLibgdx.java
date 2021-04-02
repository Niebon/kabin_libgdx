package dev.kabin.entities.impl.animation;

import dev.kabin.entities.AnimationPlayback;
import dev.kabin.entities.impl.GraphicsParametersLibgdx;

/**
 * All animation playbacks used for the libgdx implementation should extend this interface.
 *
 * @param <AnimationType> a set of animations.
 */
public interface AnimationPlaybackLibgdx<AnimationType extends Enum<AnimationType>> extends
        AnimationPlayback<AnimationType, GraphicsParametersLibgdx> {
}
